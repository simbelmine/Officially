
// Use Parse.Cloud.define to define as many cloud functions as you want.
// For example:
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

			alert("OUTside yourReligionCriteria: " + yourReligionCriteria);

	if(drinkingCriteria == 0 && religionCriteria == 0 && smokingCriteria == 0 && ethnicityCriteria == 0) {
		Parse.Cloud.run("getAllUsers", {userName:userName}, {
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
				excludeCriteriaFromAllUsers:excludeCriteriaFromAllUsers
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
		Parse.Cloud.run("getByCriteria", {
			userName:userName,
			drinkingCriteria:drinkingCriteria,
			religionCriteria:religionCriteria,
			smokingCriteria:smokingCriteria,
			ethnicityCriteria:ethnicityCriteria,
			yourReligionCriteria:yourReligionCriteria,
			yourEthnicityCriteria:yourEthnicityCriteria,
			criteriaSign:criteriaSign }, 
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

			matchingQuery.notEqualTo("loginName", userName);
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
	var getUsersQuery = new Parse.Query(Parse.User);
	getUsersQuery.notEqualTo("username", request.params.userName);
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

	alert("criteriaSign: " + criteriaSign); 
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

		getUsersQuery.doesNotMatchKeyInQuery("username", "loginName", matchingQuery);
		getUsersQuery.find({
		success: function(listResults) {
			var list = [];
			var listBool = [];
			listBool.push(excludeCriteriaFromAllUsers);
			list.push(listResults);
			list.push(listBool);

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

		getUsersQuery.matchesKeyInQuery("username", "loginName", matchingQuery);
		getUsersQuery.find({
		success: function(listResults) {
			var list = [];
			var listBool = [];
			listBool.push(excludeCriteriaFromAllUsers);
			list.push(listResults);
			list.push(listBool);

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
