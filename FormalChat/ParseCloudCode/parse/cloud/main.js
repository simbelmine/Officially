
// Use Parse.Cloud.define to define as many cloud functions as you want.
// For example:
Parse.Cloud.define("setConversation", function(request, response) {
	Parse.Cloud.useMasterKey();

	var messageId = request.params.messageId;
	var messageText = request.params.messageText;
	var senderId = request.params.senderId;
	var receiverId = request.params.receiverId;
	var senderName = request.params.senderName;
	var receiverName = request.params.receiverName;

    var UserConversationsTable = Parse.Object.extend("Conversation");
	var getConversationsQuery_1 = new Parse.Query(UserConversationsTable); 	
	getConversationsQuery_1.equalTo("senderId", senderId);
	getConversationsQuery_1.equalTo("receiverId", receiverId);
	var getConversationsQuery_2 = new Parse.Query(UserConversationsTable); 
	getConversationsQuery_2.equalTo("senderId", receiverId);
	getConversationsQuery_2.equalTo("receiverId", senderId);

	var mainQuery = Parse.Query.or(getConversationsQuery_1, getConversationsQuery_2);
	mainQuery.find({ 
		success: function(results) {
			var po;
			if(results.length > 0) {
				po = results[0];
				po.set("messageId", messageId);
				po.set("messageText", messageText);
				po.save(null, {
					success: function(savedConversationObj) {
						alert(" --- Row exists: Saved"); 

						var Message = Parse.Object.extend("Message");
						var query = new Parse.Query(Message);
						query.get(messageId, {
						  success: function(msg) {
						    // The object was retrieved successfully.
						    alert("Message was retreived successfully = " + msg.id); 	
						    alert("Conversation Id = " + po.id); 	
						    msg.set("conversationId", po.id);
						    msg.save(null, {
						    	success: function(savedMsgObj) {
									alert(" --- Msg convId: Saved"); 
									response.success(results);
						    	},
						    	error: function(savedMsgObj) {
									alert(" --- Msg convId: NOT Saved"); 
									response.error(error);
						    	}
						    });
						  },
						  error: function(msg, error) {
						    // The object was not retrieved successfully.
						    // error is a Parse.Error with an error code and message.
						    alert("Error: " + error.code + " " + error.msg); 	
						  }
						});
					},
					error: function(savedConversationObj, error) {
						alert(" --- Row exists: NOT Saved");
					}
				});
			}
			else {
				alert("Row DONT exists : " + senderId + " - " + receiverId); 
				var po = new Parse.Object("Conversation");
				po.set("senderId", senderId);
				po.set("receiverId", receiverId);
				po.set("senderName", senderName);
				po.set("receiverName", receiverName);
				po.set("messageId", messageId);
				po.set("messageText", messageText);
				po.save(null, {
					success: function(po) {
						alert("Row DONT exists: conversationId = " + po); 	
					},
					error: function(po, error) {

					}
				});
			}

			// response.success(results);
		},
		error: function(error) {
			alert("Error: " + error.code + " " + error.message); 	
    		// response.error(error);
  		}
	});

});

Parse.Cloud.define("getAllUserConversations", function(request, response) {
    var UserConversationsTable = Parse.Object.extend("Conversation");
    var userId = Parse.User.current().id;

	var getConversationsQuery_1 = new Parse.Query(UserConversationsTable); 	
	getConversationsQuery_1.equalTo("senderId", userId);
	var getConversationsQuery_2 = new Parse.Query(UserConversationsTable); 
	getConversationsQuery_2.equalTo("receiverId", userId);	

	var mainQuery = Parse.Query.or(getConversationsQuery_1, getConversationsQuery_2);
	mainQuery.descending("createdAt");
	mainQuery.limit(10);
	mainQuery.find({
		success: function(listResults) {
				var list = [];
				list.push(listResults);
				response.success(list);
			},
		error: function(error) {
				response.error("error");
	    		alert("Error: " + error.code + " " + error.message); 					
			}
		});
});

Parse.Cloud.define("clientRequest", function(request, response) {
	var userName = request.params.userName;
	var drinkingCriteria = request.params.drinkingCriteria;
	var religionCriteria = request.params.religionCriteria;
	var smokingCriteria = request.params.smokingCriteria;
	var criteriaSign = request.params.criteriaSign;
	var ethnicityCriteria = request.params.ethnicityCriteria;
	var yourReligionCriteria = request.params.religionCriteria;
	var yourEthnicityCriteria = request.params.ethnicityCriteria;
	var excludeCriteriaFromAllUsers = request.params.excludeCriteriaFromAllUsers;
	var rowsToSkip = request.params.rowsToSkip;
	var rowsLimit = request.params.rowsLimit;

	alert("Skip number of Rows: " + rowsToSkip);
			// alert("OUTside yourReligionCriteria: " + yourReligionCriteria);

	if(drinkingCriteria == 0 && religionCriteria == 0 && smokingCriteria == 0 && ethnicityCriteria == 0) {
		Parse.Cloud.run("getAllUsers", 
			{
				userName:userName,
				rowsToSkip:rowsToSkip,
				rowsLimit:rowsLimit
			}, 
			{
			success: function(results) {
				response.success(results);
			},
			error: function(error) {
				response.error("error");
	    		alert("Error: " + error.code + " " + error.message); 					
			}
		});
	}
	else if(excludeCriteriaFromAllUsers !=null && 
			excludeCriteriaFromAllUsers == true) 
	{
		// Get All results witch are not fitting the search filter (ALL RESULTS)
		alert("excludeCriteriaFromAllUsers IN if: " + excludeCriteriaFromAllUsers);
			Parse.Cloud.run("getAllUsersExcludeCriteria", {
				userName:userName,
				drinkingCriteria:drinkingCriteria,
				religionCriteria:religionCriteria,
				smokingCriteria:smokingCriteria,
				ethnicityCriteria:ethnicityCriteria,
				yourReligionCriteria:yourReligionCriteria,
				yourEthnicityCriteria:yourEthnicityCriteria,
				criteriaSign:criteriaSign,
				excludeCriteriaFromAllUsers:excludeCriteriaFromAllUsers,
				rowsToSkip:rowsToSkip,
				rowsLimit:rowsLimit
			},
			{
			success: function(results) {
				response.success(results);
			},
			error: function(error) {
				response.error("error");
	    		alert("Error: " + error.code + " " + error.message); 					
			}
		});
	}
	else {
		// Get all users fitting the search filter (MATCHES)
		Parse.Cloud.run("getByCriteria", {
			userName:userName,
			drinkingCriteria:drinkingCriteria,
			religionCriteria:religionCriteria,
			smokingCriteria:smokingCriteria,
			ethnicityCriteria:ethnicityCriteria,
			yourReligionCriteria:yourReligionCriteria,
			yourEthnicityCriteria:yourEthnicityCriteria,
			criteriaSign:criteriaSign,
			rowsToSkip:rowsToSkip,
			rowsLimit:rowsLimit
			}, 
			{
			success: function(results) {
				response.success(results);
			},
			error: function(error) {
				response.error("error");
	    		alert("Error: " + error.code + " " + error.message); 					
			}
		});
	}
});


Parse.Cloud.define("getByCriteria", function(request, response) {
	var getUsersQuery = new Parse.Query(Parse.User);
	var UserQuestionary = Parse.Object.extend("UserQuestionary");
	var matchingQuery = new Parse.Query(UserQuestionary);

	var userName = request.params.userName;
	var criteriaSign = request.params.criteriaSign;
	var drinkingCriteria = request.params.drinkingCriteria;
	var religionCriteria = request.params.religionCriteria;
	var smokingCriteria = request.params.smokingCriteria;
	var ethnicityCriteria = request.params.ethnicityCriteria;
	var yourReligionCriteria = request.params.yourReligionCriteria;
	var yourEthnicityCriteria = request.params.yourEthnicityCriteria;
	var rowsToSkip = request.params.rowsToSkip;
	var rowsLimit = request.params.rowsLimit;

	matchingQuery.notEqualTo("loginName", userName);
	matchingQuery.descending("lastSeen");

	if(rowsToSkip == null) {
		rowsToSkip = 0;
	}
	// matchingQuery.skip(rowsToSkip);
	// if(rowsLimit != null) {
	// 	alert("getByCriteria: Limit of Rows: " + rowsLimit);
	// 	matchingQuery.limit(rowsLimit);
	// }

	if(criteriaSign != null) {
		if(drinkingCriteria != null && criteriaSign == ">=") {
			// matchingQuery.notEqualTo("loginName", userName);
			matchingQuery.greaterThanOrEqualTo("yourDrinking", drinkingCriteria);
		}
		else if(drinkingCriteria != null && criteriaSign == "<=") {
			// matchingQuery.notEqualTo("loginName", userName);
			matchingQuery.lessThanOrEqualTo("yourDrinking", drinkingCriteria);
		}
		else if(smokingCriteria != null && criteriaSign == ">=") {
			// matchingQuery.notEqualTo("loginName", userName);
			matchingQuery.greaterThanOrEqualTo("yourSmoking", smokingCriteria);
		}
		else if(smokingCriteria != null && criteriaSign == "<=") {
			// matchingQuery.notEqualTo("loginName", userName);
			matchingQuery.lessThanOrEqualTo("yourSmoking", smokingCriteria);
		}
	}
	if(criteriaSign == "==") {
		if(religionCriteria != null) {
				matchingQuery.equalTo("yourReligion", yourReligionCriteria);
			}
			if(ethnicityCriteria != null) {
				matchingQuery.equalTo("yourEthnicity", yourEthnicityCriteria)
			}
		}

	if(criteriaSign == null && religionCriteria != null && 
		drinkingCriteria == null && smokingCriteria == null && 
		ethnicityCriteria == null) {
		matchingQuery.notEqualTo("loginName", userName);
		matchingQuery.equalTo("yourReligion", religionCriteria);
	}
	if(criteriaSign == null && ethnicityCriteria != null && 
		drinkingCriteria == null && smokingCriteria == null && 
		religionCriteria == null) {
		matchingQuery.notEqualTo("loginName", userName);
		matchingQuery.equalTo("yourReligion", ethnicityCriteria);
	}

	if(criteriaSign == null && drinkingCriteria != null && religionCriteria != null && 
		smokingCriteria != null) {
		matchingQuery.notEqualTo("loginName", userName);
		matchingQuery.equalTo("yourSmoking", smokingCriteria);
		matchingQuery.equalTo("yourDrinking", drinkingCriteria);
		// *** If Relligion or Ethnicity are important than add as Criteria *** //
		if(religionCriteria >= 2) {
			matchingQuery.equalTo("yourReligion", yourReligionCriteria);
		}
		if(ethnicityCriteria >= 2) {
			matchingQuery.equalTo("yourEthnicity", yourEthnicityCriteria)
		}	
	}

	getUsersQuery.matchesKeyInQuery("username", "loginName", matchingQuery);
	getUsersQuery.skip(rowsToSkip);
	if(rowsLimit != null) {
		alert("getByCriteria: Limit of Rows: " + rowsLimit);
		getUsersQuery.limit(rowsLimit);
	}
	getUsersQuery.find({
		success: function(listResults) {
			var list = [];
			list.push(listResults);
			response.success(list);
		},
		error: function(error) {
			response.error("error");
	    	alert("Error: " + error.code + " " + error.message); 					
			}
	});
});


Parse.Cloud.define("getAllUsers", function(request, response) {
	var rowsToSkip = request.params.rowsToSkip;
	var rowsLimit = request.params.rowsLimit;
	var getUsersQuery = new Parse.Query(Parse.User);
	getUsersQuery.notEqualTo("username", request.params.userName);
	getUsersQuery.descending("lastSeen");
	if(rowsToSkip == null) {
		rowsToSkip = 0;
	}
	getUsersQuery.skip(rowsToSkip);
	if(rowsLimit != null) {
		alert("getAllUsers: Limit of Rows: " + rowsLimit);
		getUsersQuery.limit(rowsLimit);
	}

	getUsersQuery.find({
		success: function(listResults) {
				var list = [];
				list.push(listResults);
				response.success(list);
			},
		error: function(error) {
				response.error("error");
	    		alert("Error: " + error.code + " " + error.message); 					
			}
		});
});

Parse.Cloud.define("getAllUsersExcludeCriteria", function(request, response) {
	var getUsersQuery = new Parse.Query(Parse.User);
	var UserQuestionary = Parse.Object.extend("UserQuestionary");
	var matchingQuery = new Parse.Query(UserQuestionary);

	var userName = request.params.userName;
	var criteriaSign = request.params.criteriaSign;
	var drinkingCriteria = request.params.drinkingCriteria;
	var religionCriteria = request.params.religionCriteria;
	var smokingCriteria = request.params.smokingCriteria;
	var ethnicityCriteria = request.params.ethnicityCriteria;
	var yourReligionCriteria = request.params.yourReligionCriteria;
	var yourEthnicityCriteria = request.params.yourEthnicityCriteria;
	var excludeCriteriaFromAllUsers = request.params.excludeCriteriaFromAllUsers;
	var rowsToSkip = request.params.rowsToSkip;
	var rowsLimit = request.params.rowsLimit;

	alert("criteriaSign: " + criteriaSign); 
	matchingQuery.descending("lastSeen");
	if(rowsToSkip == null) {
		rowsToSkip = 0;
	}

	if(criteriaSign == null) {
		//matchingQuery.notEqualTo("loginName", userName);
		matchingQuery.equalTo("yourSmoking", smokingCriteria);
		matchingQuery.equalTo("yourDrinking", drinkingCriteria);
		// *** If Relligion or Ethnicity are important than add as Criteria *** //
		if(religionCriteria >= 2) {
			matchingQuery.equalTo("yourReligion", yourReligionCriteria);
		}
		if(ethnicityCriteria >= 2) {
			matchingQuery.equalTo("yourEthnicity", yourEthnicityCriteria)
		}


		matchingQuery.skip(rowsToSkip);
		if(rowsLimit != null) {
			alert("getAllUsersExcludeCriteria: Limit of Rows: " + rowsLimit);	
			getUsersQuery.limit(rowsLimit);
		}

		getUsersQuery.doesNotMatchKeyInQuery("username", "loginName", matchingQuery);
		getUsersQuery.find({
		success: function(listResults) {
			var list = [];
			var listBool = [];
			listBool.push(excludeCriteriaFromAllUsers);
			list.push(listResults);
			list.push(listBool);

			alert("listResults, criteriaSign is Null ==== " + listResults.length); 

			response.success(list);
		},
		error: function(error) {
			response.error("error");
	    	alert("Error: " + error.code + " " + error.message); 					
			}
	});
	}
	else if(criteriaSign != null) {
		// alert("criteriaSign: " + criteriaSign);
		// alert("smokingCriteria: " + smokingCriteria);
		// alert("drinkingCriteria: " + drinkingCriteria);
		alert("yourReligionCriteria: " + yourReligionCriteria);
		// alert("yourEthnicityCriteria: " + yourEthnicityCriteria);

		 matchingQuery.notEqualTo("loginName", userName);
		if(smokingCriteria != null && criteriaSign == ">=") {
			matchingQuery.lessThan("yourSmoking", smokingCriteria);
		}
		else if(smokingCriteria != null && criteriaSign == "<=") {
			matchingQuery.greaterThan("yourSmoking", smokingCriteria);
		}
		else if(drinkingCriteria != null && criteriaSign == ">=") {
			matchingQuery.lessThan("yourDrinking", drinkingCriteria);
		}
		else if(drinkingCriteria != null && criteriaSign == "<=") {
			matchingQuery.greaterThan("yourDrinking", drinkingCriteria);
		}

		else if(criteriaSign == "!=") {
			if(religionCriteria != null) {
				alert("----yourReligionCriteria: " + yourReligionCriteria);
				matchingQuery.notEqualTo("yourReligion", yourReligionCriteria);
			}
			if(ethnicityCriteria != null) {
				matchingQuery.notEqualTo("yourEthnicity", yourEthnicityCriteria)
			}
		}

	matchingQuery.skip(rowsToSkip);
	if(rowsLimit != null) {
		alert("getAllUsersExcludeCriteria: Limit of Rows: " + rowsLimit);	
		getUsersQuery.limit(rowsLimit);
	}

		getUsersQuery.matchesKeyInQuery("username", "loginName", matchingQuery);
		getUsersQuery.find({
		success: function(listResults) {
			var list = [];
			var listBool = [];
			listBool.push(excludeCriteriaFromAllUsers);
			list.push(listResults);
			list.push(listBool);
			alert("listResults, criteriaSign is NOT Null ==== " + listResults.length); 


			response.success(list);
		},
		error: function(error) {
			response.error("error");
	    	alert("Error: " + error.code + " " + error.message); 					
			}
	});
	}	

	
});

		// matchingQuery.find({
		// 	success: function(listResults) {
		// 		var names = [];
		// 		for(var i = 0; i < listResults.length; i++) {
		// 			names.push(listResults[i].get("loginName"));
		// 		}
		// 		response.success(names);
		// 	},
		// 	error: function(error) {
		// 		response.error("error");
	 //    		alert("Error: " + error.code + " " + error.message);
		// 	}
		// });
	
	// var getUsersQuery = new Parse.Query(Parse.User);
	// getUsersQuery.notEqualTo("username", userName);

	// var UserQuestionary = Parse.Object.extend("UserQuestionary");
	// var getQuestionaryNumberQuery = new Parse.Query(UserQuestionary);
	// getQuestionaryNumberQuery.matchesKeyInQuery("loginName", "username", getUsersQuery);
	// getQuestionaryNumberQuery.find({
	// 	success: function(listResults) {
	// 		response.success(listResults.length);
	// 	},
	// 	error: function(error) {
	// 		response.error("error");
 //    		alert("Error: " + error.code + " " + error.message);
	// 	}
	// }); 
