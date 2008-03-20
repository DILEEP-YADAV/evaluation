/**
 * $Id$
 * $URL$
 * SetupEvalBean.java - evaluation - Mar 18, 2008 4:38:20 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.tool;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupService;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.model.EvalAssignHierarchy;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.tool.locators.EmailTemplateWBL;
import org.sakaiproject.evaluation.tool.locators.EvaluationBeanLocator;
import org.sakaiproject.evaluation.utils.ArrayUtils;
import org.sakaiproject.evaluation.utils.EvalUtils;

import uk.org.ponder.conversion.StringArrayParser;
import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;


/**
 * This action bean helps with the evaluation setup process where needed,
 * this is a pea
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class SetupEvalBean {

   /**
    * This should be set to true while we are creating an evaluation
    */
   public boolean creatingEval = false;
   /**
    * This should be set to the evalId we are currently working with
    */
   public Long evaluationId;
   /**
    * This should be set to the templateId we are assigning to the evaluation
    */
   public Long templateId;
   /**
    * This should be set to the emailTemplateId we are assigning to the evaluation
    */
   public Long emailTemplateId;
   /**
    * This is set to the type of email template when resetting the evaluation to use default templates
    */
   public String emailTemplateType;

   /**
    * the selected groups ids to bind to this evaluation when creating it
    */
   public String[] selectedGroupIDs = new String[] {};
   /**
    * the selected hierarchy nodes to bind to this evaluation when creating it
    */
   public String[] selectedHierarchyNodeIDs = new String[] {};

   /*
    * TODO FIXME Bug that is fixed in next version of RSF. See
    * EvaluationAssignConfirmProducer around line: 196
    */
   public void setSelectedGroupIDsWithPucArray(String toparse) {
      selectedGroupIDs = (String[]) StringArrayParser.instance.parse(toparse);
   }

   public void setSelectedHierarchyNodeIDsWithPucArray(String toparse) {
      selectedHierarchyNodeIDs = (String[]) StringArrayParser.instance.parse(toparse);
   }

   private EvalEvaluationService evaluationService;
   public void setEvaluationService(EvalEvaluationService evaluationService) {
      this.evaluationService = evaluationService;
   }

   private EvalExternalLogic externalLogic;
   public void setExternalLogic(EvalExternalLogic externalLogic) {
      this.externalLogic = externalLogic;
   }

   private ExternalHierarchyLogic hierarchyLogic;
   public void setExternalHierarchyLogic(ExternalHierarchyLogic logic) {
      this.hierarchyLogic = logic;
   }

   private EvalEvaluationSetupService evaluationSetupService;
   public void setEvaluationSetupService(EvalEvaluationSetupService evaluationSetupService) {
      this.evaluationSetupService = evaluationSetupService;
   }

   private EvalAuthoringService authoringService;
   public void setAuthoringService(EvalAuthoringService authoringService) {
      this.authoringService = authoringService;
   }

   private EvaluationBeanLocator evaluationBeanLocator;
   public void setEvaluationBeanLocator(EvaluationBeanLocator evaluationBeanLocator) {
      this.evaluationBeanLocator = evaluationBeanLocator;
   }

   private EmailTemplateWBL emailTemplateWBL;
   public void setEmailTemplateWBL(EmailTemplateWBL emailTemplateWBL) {
      this.emailTemplateWBL = emailTemplateWBL;
   }

   private TargettedMessageList messages;
   public void setMessages(TargettedMessageList messages) {
      this.messages = messages;
   }

   private Locale locale;
   public void setLocale(Locale locale){
      this.locale=locale;
   }


   DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);
   public void init() {
      df = DateFormat.getDateInstance(DateFormat.LONG, locale);
   }


   // Action bindings

   /**
    * Handles removal action from the remove eval view
    */
   public String removeEvalAction(){
      if (evaluationId == null) {
         throw new IllegalArgumentException("evaluationId and emailTemplateId cannot be null");
      }
      EvalEvaluation eval = evaluationService.getEvaluationById(evaluationId);
      evaluationSetupService.deleteEvaluation(evaluationId, externalLogic.getCurrentUserId());
      messages.addMessage( new TargettedMessage("controlevaluations.delete.user.message",
            new Object[] { eval.getTitle() }, TargettedMessage.SEVERITY_INFO));
      return "success";
   }

   /**
    * Handles saving and assigning email templates to an evaluation,
    * can just assign the email template if the emailTemplateId is set or
    * will save and assign the one in the locator
    */
   public String saveAndAssignEmailTemplate() {
      if (evaluationId == null) {
         throw new IllegalArgumentException("evaluationId and emailTemplateId cannot be null");
      }

      EvalEmailTemplate emailTemplate = null;
      if (emailTemplateId == null) {
         // get it from the locator
         emailTemplateWBL.saveAll();
         emailTemplate = emailTemplateWBL.getCurrentEmailTemplate();
      } else {
         // just load up and assign the template and do not save it
         emailTemplate = evaluationService.getEmailTemplate(emailTemplateId);
      }

      // assign to the evaluation
      evaluationSetupService.assignEmailTemplate(emailTemplate.getId(), evaluationId, null, externalLogic.getCurrentUserId());

      // user message
      EvalEvaluation eval = evaluationService.getEvaluationById(evaluationId);
      messages.addMessage( new TargettedMessage("controlemailtemplates.template.assigned.message",
            new Object[] { emailTemplate.getType(), emailTemplate.getSubject(), eval.getTitle() }, 
            TargettedMessage.SEVERITY_INFO));
      return "successAssign";
   }

   /**
    * Handles resetting the evaluation to use the default template
    */
   public String resetToDefaultEmailTemplate() {
      if (evaluationId == null 
            || emailTemplateType == null) {
         throw new IllegalArgumentException("evaluationId and emailTemplateType cannot be null");
      }

      // reset to default email template
      evaluationSetupService.assignEmailTemplate(null, evaluationId, emailTemplateType, externalLogic.getCurrentUserId());

      return "successReset";
   }



   // NOTE: these are the simple navigation methods
   // 4 steps to create an evaluation: 1) Create -> 2) Settings -> 3) Assign -> 4) Confirm/Save

   /**
    * Completed the initial creation page where the template is chosen
    */
   public String completeCreateAction() {
      // set the template on the evaluation in the bean locator (TODO - get rid of this)
      if (templateId == null) {
         throw new IllegalStateException("The templateId is null, it must be set so it can be assigned to the new evaluation");
      }
      EvalEvaluation eval = evaluationBeanLocator.getCurrentEval();
      if (eval == null) {
         throw new IllegalStateException("The evaluation cannot be retrieved from the bean locator, critical failure");
      }
      EvalTemplate template = authoringService.getTemplateById(templateId);
      eval.setTemplate(template);

      // save the new evaluation
      evaluationBeanLocator.saveAll();
      return "evalSettings";
   }

   /**
    * Updated or initially set the evaluation settings
    */
   public String completeSettingsAction() {
      // set the template on the evaluation in the bean locator if not null
      if (templateId != null) {
         EvalEvaluation eval = evaluationBeanLocator.getCurrentEval();
         if (eval != null) {
            EvalTemplate template = authoringService.getTemplateById(templateId);
            eval.setTemplate(template);
         }
      }

      evaluationBeanLocator.saveAll();

      EvalEvaluation eval = evaluationBeanLocator.getCurrentEval();
      String destination = "controlEvals";
      if (EvalConstants.EVALUATION_STATE_PARTIAL.equals(eval.getState())) {
         destination = "evalAssign";
      } else {
         messages.addMessage( new TargettedMessage("evalsettings.updated.message",
               new Object[] { eval.getTitle() }, TargettedMessage.SEVERITY_INFO));
      }
      return destination;
   }

   // NOTE: There is no action for the 3) assign step because that one just passes the data straight to the confirm view

   // TODO - how do we handle removing assignments? (Currently not supported)

   /**
    * Complete the creation process for an evaluation (view all the current settings and assignments and create eval/assignments)
    */
   public String completeConfirmAction() {
      if (evaluationId == null) {
         throw new IllegalArgumentException("evaluationId and emailTemplateId cannot be null");
      }
      // make sure that the submitted nodes are valid and populate the nodes list
      Set<EvalHierarchyNode> nodes = null;
      if (selectedHierarchyNodeIDs.length > 0) {
         nodes = hierarchyLogic.getNodesByIds(selectedHierarchyNodeIDs);
         if (nodes.size() != selectedHierarchyNodeIDs.length) {
            throw new IllegalArgumentException("Invalid set of hierarchy node ids submitted which "
                  + "includes node Ids which are not in the hierarchy: " + ArrayUtils.arrayToString(selectedHierarchyNodeIDs));
         }
      } else {
         nodes = new HashSet<EvalHierarchyNode>();
      }

      // at least 1 node or group must be selected
      if (selectedGroupIDs.length == 0 
            && nodes.isEmpty() ) {
         messages.addMessage( new TargettedMessage("assigneval.invalid.selection",
               new Object[] {}, TargettedMessage.SEVERITY_ERROR));
         return "fail";
      }

      EvalEvaluation eval = evaluationService.getEvaluationById(evaluationId);
      if (EvalConstants.EVALUATION_STATE_PARTIAL.equals(eval.getState())) {
         // save eval and assign groups

         // save the new evaluation state (moving from partial)
         eval.setState( EvalUtils.getEvaluationState(eval, true) ); // set the state according to dates only
         evaluationSetupService.saveEvaluation(eval, externalLogic.getCurrentUserId());

         // NOTE - this allows the evaluation to be saved with zero assign groups if this fails

         // expand the hierarchy to include all nodes below this one
         Set<String> allNodeIds = hierarchyLogic.getAllChildrenNodes(nodes, true);

         // save all the assignments (hierarchy and group)
         List<EvalAssignHierarchy> assignedHierList = 
            evaluationSetupService.addEvalAssignments(evaluationId, 
                  allNodeIds.toArray(new String[allNodeIds.size()]), selectedGroupIDs);

         // failsafe check (to make sure we are not creating an eval with no assigned groups)
         if (assignedHierList.isEmpty()) {
            evaluationSetupService.deleteEvaluation(evaluationId, externalLogic.getCurrentUserId());
            throw new IllegalStateException("Invalid evaluation created with no assignments! Destroying evaluation: " + evaluationId);
         }

         messages.addMessage( new TargettedMessage("controlevaluations.create.user.message",
               new Object[] { eval.getTitle(), df.format(eval.getStartDate()) }, 
               TargettedMessage.SEVERITY_INFO));
      } else {
         // just assigning groups
         // expand the hierarchy to include all nodes below this one
         Set<String> allNodeIds = hierarchyLogic.getAllChildrenNodes(nodes, true);

         // save all the assignments (hierarchy and group)
         evaluationSetupService.addEvalAssignments(evaluationId, 
               allNodeIds.toArray(new String[allNodeIds.size()]), selectedGroupIDs);
      }
      return "controlEvals";
   }


   // NOTE: these are the support methods

   /**
    * Turn a boolean selection map into an array of the keys
    * 
    * @param booleanSelectionMap a map of string -> boolean (from RSF bound booleans)
    * @return an array of the keys where boolean is true
    */
   public static String[] makeArrayFromBooleanMap(Map<String, Boolean> booleanSelectionMap) {
      List<String> hierNodeIdList = new ArrayList<String>();
      for (String hierNodeID: booleanSelectionMap.keySet()) {
         if (booleanSelectionMap.get(hierNodeID) == true) {
            hierNodeIdList.add(hierNodeID);
         }
      }
      String[] selectedHierarchyNodeIds = hierNodeIdList.toArray(new String[] {});
      return selectedHierarchyNodeIds;
   }


}