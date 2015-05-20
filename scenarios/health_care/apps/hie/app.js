var express = require('express');
var http = require('http');
var path = require('path');
var favicon = require('static-favicon');
var logger = require('morgan');
var bodyParser = require('body-parser');
var session = require('express-session');

var routes = require('./routes');

var app = express();
if(process.argv.length == 2) {
    app.listen(3004, function() {
        app.this_http_port = 3004;
        console.log("HIE's app is listening to port 3004");
    });
}
else if(process.argv.length == 3) {
    var target_http_port = parseInt(process.argv[2]);
    app.this_http_port = target_http_port;
    console.log("HTTP Port : " + target_http_port);

    app.listen(target_http_port, function() {
        console.log("HIE's app is listening to port " + target_http_port);
    });
}

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'jade');

app.use(favicon());
app.use(logger('dev'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded());
app.use(express.static(path.join(__dirname, 'public')));

// session setup
app.use(session({
  resave: false, // don't save session if unmodified
  saveUninitialized: false, // don't create session until something stored
  // TODO allocate secret which is secure random
  secret: 'shhhh, very secret'
}));

// common log
// TODO finish this part
app.use(function(req, res,next) {
    console.log("");
    console.log("<<Access to " + req.url + ">>");
    console.log("<<Host name  : " + req.headers.host + ">>");
  
    /*
    var idx = req.headers.host.lastIndexOf(":");
    var port = req.headers.host.substring(idx + 1, req.headers.host.length);
    console.log("port number: " + port);
    */
    //console.log("test time: " + req.session.cookie.maxAge);
    next();
});

app.use(app.router);

app.get('/', routes.index);
app.post('/update_hie_record', routes.update_hie_record);

// This is for debugging. Records should not be transmitted as plaintext.
// This enables to confirm whether the record is inserted correctly
app.get('/list_hie_record', routes.list_hie_record);

app.get('/get_read_param', routes.get_read_param);
app.get('/get_update_info', routes.get_update_info);
app.post('/authenticate', routes.authenticate);
app.post('/check_hash_validity', routes.check_hash_validity);
app.get('/logout', routes.logout);

app.get('/get_result', routes.get_result);

// Revocation functionalities
app.post('/update_read_info', routes.update_read_info);

/// catch 404 and forwarding to error handler
app.use(function(req, res, next) {
    var err = new Error('Not Found');
    err.status = 404;
    next(err);
});

/// error handlers

// development error handler
// will print stacktrace
if (app.get('env') === 'development') {
    app.use(function(err, req, res, next) {
        res.render('error', {
            message: err.message,
            error: err
        });
    });
}

// production error handler
// no stacktraces leaked to user
app.use(function(err, req, res, next) {
    res.render('error', {
        message: err.message,
        error: {}
    });
});


module.exports = app;
