/******************************************************************************
 * EvalEmailLogic.java - created by aaronz@vt.edu on Dec 27, 2006
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - primary
 * 
 *****************************************************************************/

package org.sakaiproject.evaluation.logic;

import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.constant.EvalConstants;

/**
 * Handles all logic associated with processing email and email templates,
 * also handles sending emails
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface EvalEmailsLogic {

	// EMAIL TEMPLATES

	/**
	 * Save or update an email template, don't forget to associate it
	 * with the evaluation and save that separately<br/> 
	 * <b>Note:</b> cannot update template if used in at least one 
	 * evaluation that is not in queue<br/>
	 * Use {@link #canControlEmailTemplate(String, Long, Long)} or
	 * {@link #canControlEmailTemplate(String, Long, String)} to check
	 * if user can update this template and avoid possible exceptions
	 * 
	 * @param EmailTemplate emailTemplate object to be saved
	 * @param userId the internal user id (not username)
	 */
	public void saveEmailTemplate(EvalEmailTemplate emailTemplate, String userId);

	/**
	 * Get a default email template by type, use the defaults as the basis for all
	 * new templates that are created by users
	 * 
	 * @param emailTemplateTypeConstant a constant, use the EMAIL_TEMPLATE constants from 
	 * {@link org.sakaiproject.evaluation.model.constant.EvalConstants} to indicate the type
	 * @return the default email template matching the supplied type
	 */
	public EvalEmailTemplate getDefaultEmailTemplate(int emailTemplateTypeConstant);


	// PERMISSIONS

	/**
	 * Check if a user can control (create, modify, or delete) an email template for the
	 * given evaluation of the given template type, takes into account the permissions and 
	 * current state of the evaluation
	 * 
	 * @param userId the internal user id (not username)
	 * @param evaluationId the id of an EvalEvaluation object
	 * @param emailTemplateTypeConstant a constant, use the EMAIL_TEMPLATE constants from 
	 * {@link org.sakaiproject.evaluation.model.constant.EvalConstants} to indicate the type
	 * @return true if the user can control the email template at this time, false otherwise
	 */
	public boolean canControlEmailTemplate(String userId, Long evaluationId, int emailTemplateTypeConstant);

	/**
	 * Check if a user can control (modify or delete) a given 
	 * email template for the given evaluation,
	 * takes into account the ownership, permissions, and current state of the evaluation
	 * 
	 * @param userId the internal user id (not username)
	 * @param evaluationId the id of an EvalEvaluation object
	 * @param emailTemplateId the id of an EvalEmailTemplate object
	 * @return true if the user can control the email template at this time, false otherwise
	 */
	public boolean canControlEmailTemplate(String userId, Long evaluationId, Long emailTemplateId);


	// NOTIFICATION methods

	/**
	 * Send notifications to evaluatees (and owner if desired) that a new evaluation
	 * has been created, includes links directly to add their own questions and details
	 * about the dates for the evaluation
	 * 
	 * @param evaluationId the id of an EvalEvaluation object
	 * @param includeOwner if true then send an email to the owner (creator of this evaluation) also, else do not include the owner
	 */
	public void sendEvalCreatedNotifications(Long evaluationId, boolean includeOwner);

	/**
	 * Send notifications to evaluators that there is an evaluation ready for them to take
	 * and includes information about the evaluation (dates), also includes links to
	 * take the evaluation in "one-click" (i.e. link directly to the take_eval page)
	 * 
	 * @param evaluationId the id of an EvalEvaluation object
	 * @param includeEvaluatees if true, include notification to the evaluatees (probably instructors) that
	 * if they have not opted into an evaluation which is opt-in, otherwise this does nothing
	 */
	public void sendEvalAvailableNotifications(Long evaluationId, boolean includeEvaluatees);

	/**
	 * Send reminder notifications to all users who are taking an evaluation,
	 * these include a direct link to the evaluation and information about the
	 * context being evaluated and the dates of the evaluation, 
	 * uses the reminder template and the users to include can be controlled
	 * 
	 * @param evaluationId the id of an EvalEvaluation object
	 * @param includeConstant a constant to indicate what users should receive the notification, EMAIL_INCLUDE from {@link EvalConstants}
	 */
	public void sendEvalReminderNotifications(Long evaluationId, String includeConstant);

	/**
	 * Send notifications that the evaluation is now complete and the results are viewable,
	 * includes stats for the evaluation (response rate, etc.) and links directly to
	 * the results page for this evaluation, owner of the evaluation is always included
	 * in the notification
	 * 
	 * @param evaluationId the id of an EvalEvaluation object
	 * @param includeEvaluatees if true, include notifications to all evaluated users
	 * @param includeAdmins if true, include notifications to all admins above the contexts and
	 * eval groups evaluated in this evaluation, otherwise include evaluatees only
	 */
	public void sendEvalResultsNotifications(Long evaluationId, boolean includeEvaluatees, boolean includeAdmins);

}
