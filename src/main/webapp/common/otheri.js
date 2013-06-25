var SUCCESS = 1;
var FAILURE = -1;
var FAILURE_NETWORK = -100;

function api($http, $url, $domain, $method, $content, $callback) {
	var req = new Object();
	req.timestamp = new Date().getMilliseconds();
	req.domain = $domain;
	req.method = $method;
	req.content = $content;
	req.verify = "verify";

	$http({
		"method" : "POST",
		"url" : $url,
		"data" : JSON.stringify(req)
	}).success(function(data, status, headers, config) {
		$callback.onResult(data);
	}).error(function(data, status, headers, config) {
		var resp = new Object();
		resp.timestamp = new Date().getMilliseconds();
		resp.domain = req.domain;
		resp.method = req.method;
		resp.result = FAILURE_NETWORK;
		resp.content = data;

		// 网络错误
		$callback.onResult(data);
	});
}