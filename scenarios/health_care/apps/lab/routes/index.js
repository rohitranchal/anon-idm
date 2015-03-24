var common = require('../common.js');

/* request in promise format */
var request = function(method, target) {
    return new Promise(function(resolve, reject) {
       if(method.toLowerCase() == 'post') {
            require('request').post(target, function(error, response, body) {
                if(error) reject(error);
                else resolve({ "response": response, "body": body });
            });
       }
       else if(method.toLowerCase() == 'get') {
           require('request').get(target, function(error, response, body) {
               if(error) reject(error); 
               else resolve({ "response": response, "body": body });
           });
       }
       else {
           reject("Incorrect method!");
       }
    });
};

/* GET: home page. */
exports.index = function(req, res){
    res.render('index', { title: 'Express' });
};

/* GET: get register_record_page */
exports.register_record_page = function(req, res) {
    console.log('reguster_record_page is called');
    res.render('register_record_page');
}

/* GET: get public params from claimsdefs for record id 
 */
exports.get_parameters = function(req, res) {
    var param_names = null;
    var OwnerName = null;
    var ReadName = null;

    // 1. First, get names of claimdefs for the corresponding record id
    request('get', 'http://localhost:3001/param_names/' + req.params.id)
    .then(function(result) {
        param_names = JSON.parse(result.body)[0];
        OwnerName = param_names.OwnerName;
        ReadName = param_names.ReadName;
        console.log("OwnerName:" + OwnerName);
        console.log("ReadName: " + ReadName);

        // 2. Second, with this name, fetch claim defs and extract public defs
        var queue = [];
        queue.push(get_claim_and_extract_public_params(OwnerName));
        queue.push(get_claim_and_extract_public_params(ReadName));
        return common.promise.all(queue);
    })
    // 3. TODO
    .then(function(result) {
        console.log(result[0]);
        console.log(result[1]);
        //TODO
        public_params = { "owner": result[0], "read": result[1] };
        res.send("Still working!");
    }, function(error) {
        console.log("Errors: " + error);
    });
}

var get_claim_and_extract_public_params = function(name) {
    return request('get', 'http://localhost:3001/claimdef/' + name)
        .then(function(result) {
            console.log(result.body);
            return extract_public_params_promise(result.body);
        });
};

var extract_public_params_promise = function(ip) {
    return new Promise(function(resolve, conflict) {
        common.lab.extractPublicParams(ip, function(err, res) {
            if(err) reject(err);
            else resolve(res);
        });
    });
};

