/*
 * Created on 23 Jan 2007
 */
package org.sakaiproject.evaluation.tool;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalItemsLogic;
import org.sakaiproject.evaluation.logic.EvalTemplatesLogic;
import org.sakaiproject.evaluation.model.EvalTemplate;

/*
 * A "Local DAO" to focus dependencies and centralise fetching logic for the
 * Template views.
 */

public class LocalTemplateLogic {
  private EvalItemsLogic itemsLogic;

  public void setItemsLogic(EvalItemsLogic itemsLogic) {
    this.itemsLogic = itemsLogic;
  }

  private EvalTemplatesLogic templatesLogic;

  public void setTemplatesLogic(EvalTemplatesLogic templatesLogic) {
    this.templatesLogic = templatesLogic;
  }

  private EvalExternalLogic external;

  public void setExternal(EvalExternalLogic external) {
    this.external = external;
  }

  public EvalTemplate fetchTemplate(Long templateId) {
    return templatesLogic.getTemplateById(templateId);
  }

  public List fetchTemplateItems(Long templateId) {
    if (templateId == null) {
      return new ArrayList();
    }
    else {
    return itemsLogic.getTemplateItemsForTemplate(templateId, external
        .getCurrentUserId());
    }
  }
  
  public void saveTemplate(EvalTemplate tosave) {
    templatesLogic.saveTemplate(tosave, external.getCurrentUserId());
  }
  
  public EvalTemplate newTemplate() {
    EvalTemplate currTemplate = new EvalTemplate(new Date(), external.getCurrentUserId(),
        null, null, Boolean.FALSE);
    return currTemplate;
  }
}
