angular.module('tbwatcher', []).config(
		[ '$routeProvider', function($routeProvider) {
			$routeProvider.when('/seeds', {
				templateUrl : 'seed-list.html',
				controller : SeedListCtrl
			}).otherwise({
				redirectTo : '/seeds'
			});
		} ]);