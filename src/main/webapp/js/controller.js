function ServiceCtrl($scope, $http) {

	$scope.serverState = "...";

	$scope.start = function() {
		api($http, "/tbwatcher/service", "tbwatcher", "startServer", "", {
			onResult : function(resp) {
				if (resp.result == SUCCESS) {
					$scope.serverState = "started.";
				} else {
					alert(resp.content);
					$scope.serverState = "...";
				}
			}
		});
	};

	$scope.stop = function() {
		api($http, "/tbwatcher/service", "tbwatcher", "stopServer", "", {
			onResult : function(resp) {
				if (resp.result == SUCCESS) {
					$scope.serverState = "stoped.";
				} else {
					alert(resp.content);
					$scope.serverState = "...";
				}
			}
		});
	};

}

function SeedListCtrl($scope, $http) {

	$scope.seedUrl = "";
	$scope.seedDesc = "";

	// getSeed
	api($http, "/tbwatcher/service", "tbwatcher", "getSeeds", "", {
		onResult : function(resp) {
			if (resp.result == SUCCESS) {
				$scope.seeds = JSON.parse(resp.content);
			} else {
				alert(resp.content);
			}
		}
	});

	$scope.newSeed = function() {
		var content = new Object();
		if ($scope.seedUrl.length > 0) {
			content.url = $scope.seedUrl;
			content.desc = $scope.seedDesc;
			api($http, "/tbwatcher/service", "tbwatcher", "newSeed", JSON
					.stringify(content), {
				onResult : function(resp) {
					if (resp.result == SUCCESS) {
						alert("add seed successed");
					} else {
						alert(resp.content);
					}
				}
			});
		} else {
			alert("Seed Url is empty.");
		}
	}

	$scope.removeSeed = function($url) {
		var content = new Object();
		content.url = $url;
		api($http, "/tbwatcher/service", "tbwatcher", "removeSeed", JSON
				.stringify(content), {
			onResult : function(resp) {
				if (resp.result == SUCCESS) {
					alert("remove seed successed");
				} else {
					alert(resp.content);
				}
			}
		});
	}

	$scope.compare = function($seed) {
		var content = new Object();
		content.url = $seed.url;
		api($http, "/tbwatcher/service", "tbwatcher", "compare", JSON
				.stringify(content), {
			onResult : function(resp) {
				if (resp.result == SUCCESS) {
//					alert(resp.content);
					$seed.compare = JSON.parse(resp.content);
				} else {
					alert(resp.content);
				}
			}
		});
	}

	$scope.saveCurrentSample = function($sample) {
		var content = new Object();
		content.sample = $sample;
		api($http, "/tbwatcher/service", "tbwatcher", "saveCurrentSample", JSON
				.stringify(content), {
			onResult : function(resp) {
				if (resp.result == SUCCESS) {
					alert("saveCurrentSample success");
				} else {
					alert(resp.content);
				}
			}
		});
	}
}
