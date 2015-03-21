var request = require('request');

/* GET home page. */
exports.index = function(req, res){
  res.render('index', { title: 'Express' });
};

exports.get_parameters = function(req, res) {
    request.get('http://localhost:3001/param_names/' + req.params.id,
        function(error, response, body) {
            if(error) {
                
            }
            else {
               console.log(response); 
               res.send(JSON.parse(body));
               console.log(body);
            }
        });
}
