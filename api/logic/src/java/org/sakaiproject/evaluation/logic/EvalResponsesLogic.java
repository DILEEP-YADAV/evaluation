/**
 * $Id: EvalResponsesLogic.java 1000 Dec 24, 2006 12:07:31 AM azeckoski $
 * $URL: https://source.sakaiproject.org/contrib $
 * EvalResponsesLogic.java - evaluation - Dec 24, 2006 12:07:31 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic;

import java.util.List;
import java.util.Set;

import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalResponse;


/**
 * Handles all logic associated with responses and answers,
 * Responses can be complete or incomplete depending on if the user is taking the evaluation currently (incomplete response)
 * or has finished taking the evaluation (complete)
 * (Note for developers - do not modify this without permission from the project lead)
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface EvalResponsesLogic {
	
	/**
	 * Get a response by its unique id<br/>
	 * A response represents a single user response to an evaluation in a specific evalGroupId<br/>
	 * Note: this should mostly be used for OTP and not for normal fetching which
	 * should use the other methods in this API
	 * 
	 * @param responseId the id of an EvalResponse object
	 * @return an {@link EvalResponse} object or null if not found
	 */
	public EvalResponse getResponseById(Long responseId);

   /**
    * Get the response for the supplied evaluation and group for the supplied user<br/>
    * If there is no response yet then one is created, if the response exists then it is 
    * returned, this guarantees to either return a response or die if the inputs are invalid
    * @param evaluationId the unique id for an {@link EvalEvaluation}
    * @param userId the internal user id (not username)
    * @param evalGroupId the internal evalGroupId (represents a site or group)
    * @return an {@link EvalResponse} object
    */
   public EvalResponse getEvaluationResponseForUserAndGroup(Long evaluationId, String userId, String evalGroupId);

	/**
	 * Get the responses for the supplied evaluations for this user<br/>
	 * Note that this can return multiple responses in the case where an evaluation
	 * is assigned to multiple contexts that this user is part of, do not assume
	 * the order is always the same<br/>
	 * <b>Note:</b> If you just need the count then use the much faster
	 * {@link #countResponses(Long, String, Boolean)}
	 * 
	 * @param userId the internal user id (not username)
	 * @param evaluationIds an array of the ids of EvalEvaluation objects
	 * @param completed if true only return the completed responses, if false only return the incomplete responses,
	 * if null then return all responses
	 * @return a List of EvalResponse objects
	 */
	public List<EvalResponse> getEvaluationResponses(String userId, Long[] evaluationIds, Boolean completed);

	/**
	 * Count the number of responses for an evaluation,
	 * can count responses for an entire evaluation regardless of eval group
	 * or just responses for a specific group
	 * 
	 * @param evaluationId the id of an EvalEvaluation object
	 * @param evalGroupId the internal evalGroupId (represents a site or group),
	 * if null, include count for all eval groups
    * @param completed if true only return the completed responses, if false only return the incomplete responses,
    * if null then return all responses
	 * @return the count of associated responses
	 */
	public int countResponses(Long evaluationId, String evalGroupId, Boolean completed);


   /**
    * Get a set of user ids of users who are in an EvalGroup assigned to
    * this EvalEvaluation and permitted to take the evaluation but have 
    * not yet responded.
    * TODO - Move this to another logic package to avoid circular logic chain -AZ
    * TODO - add unit tests -AZ
    * 
    * @param evaluationId the unique id for an {@link EvalEvaluation}
    * @param evalGroupId the internal evalGroupId (represents a site or group)
    * @return a set of internal user ids
    */
   public Set<String> getNonResponders(Long evaluationId, String evalGroupId);


	/**
	 * Get the answers associated with this item and with a response to this evaluation,
	 * (i.e. item answers submitted as part of a response to the given evaluation) within
	 * the given evalGroupsIds, only gets answers from completed responses
	 * 
	 * @param itemId the id of an EvalItem object
	 * @param evaluationId the id of an EvalEvaluation object
	 * @param evalGroupIds the internal eval group ids (represents a site or group),
	 * if null, include count for all eval groups for this evaluation
	 * @return a list of EvalAnswer objects
	 */
	public List<EvalAnswer> getEvalAnswers(Long itemId, Long evaluationId, String[] evalGroupIds);

	/**
	 * Get the response ids associated with an evaluation and particular eval groups,
	 * you can choose to get all response ids or only the ones for complete/incomplete responses
	 * 
	 * @param evaluationId the id of an EvalEvaluation object
	 * @param evalGroupIds the internal eval group ids (represents a site or group),
	 * if null or empty array, include count for all eval groups for this evaluation
    * @param completed if true only return the completed responses, if false only return the incomplete responses,
    * if null then return all responses
	 * @return a list of response ids, in order by response id
	 */
	public List<Long> getEvalResponseIds(Long evaluationId, String[] evalGroupIds, Boolean completed);

	/**
	 * Saves a single response from a single user with all associated Answers,
	 * checks to make sure this user has not already saved this response or
	 * makes sure they are allowed to overwrite it, saves all associated
	 * answers at the same time to make sure the transaction succeeds<br/>
	 * This will also check to see if the user is allowed to save this response
	 * based on the current evaluation state, 
	 * also handles locking of associated evaluations<br/>
	 * When a user is taking an evaluation you should first create a response
	 * without any answers and with the end time set to null, once the user
	 * submits the evaluation you should save the response with the answers and
	 * with the end time set<br/>
	 * <b>Note:</b> You can only change the Answers and the start/end times when saving
	 * an existing response
	 * <b>Note:</b> You should set the end time to indicate that the response is
	 * complete, responses without an endtime are considered partially complete
	 * and should be ignored
	 * 
	 * @param response the response object to save, should be filled with answers
	 * @param userId the internal user id (not username)
	 */
	public void saveResponse(EvalResponse response, String userId);


	// PERMISSIONS

	/**
	 * Does a simple permission check to see if a user can modify a response, 
	 * also checks the evaluation state (only active or due states allow modify)<br/> 
	 * Does NOT check if the user can take this evaluation,
	 * use canTakeEvaluation in EvalEvaluationsLogic to check that<br/>
	 * <b>Note:</b> Responses can never be removed via the APIs<br/>
	 * <b>Note:</b> Any checks to see if a user can
	 * take an evaluation should be done with canTakeEvaluation() in
	 * the EvalEvaluationsLogic API
	 * 
	 * @param userId the internal user id (not username)
	 * @param responseId the id of an EvalResponse object
	 * @return true if the user can modify this response, false otherwise
	 */
	public boolean canModifyResponse(String userId, Long responseId);

}
