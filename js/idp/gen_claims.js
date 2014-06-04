var request  = require('request');

for(var i = 0; i < 1000; i++) {

	request.post('http://localhost:3000/claimdef_process', {form:{name:'claim' + i, description: 'claim_description' + i}}, function (error, response, body) {
		
	});
}