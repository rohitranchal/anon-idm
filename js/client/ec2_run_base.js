var request  = require('request');


//Generate request value for "student"
var start = new Date().getTime();

request.post('http://ec2-54-82-231-14.compute-1.amazonaws.com:8001/auth_empty', {form:{request:''}}, function (error, response, body) {
	if (!error && response.statusCode == 200) {

		//Make request to operation
		request.post('http://ec2-54-82-231-14.compute-1.amazonaws.com:8001/operation', {form:{session_key:body}}, function (error2, response2, body2) {
			var end = new Date().getTime();

			console.log(body2 + ' : ' + (end - start));
		});

	}
});
