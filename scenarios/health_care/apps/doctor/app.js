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
    app.listen(3002, function() {
        app.this_http_port = 3002;
        console.log("doctor's app is listening to port 3002");
    });
}
else if(process.argv.length == 3) {
    var target_http_port = parseInt(process.argv[2]);
    //var target_https_port = parseInt(process.argv[3]);
    app.this_http_port = target_http_port;
    console.log("HTTP Port : " + target_http_port);
    //console.log("HTTPS Port: " + target_https_port);

    /*
    npm install --save secure-random // version 1.1.1
    var bytes = secureRandom(10) //return an Array of 10 bytes 
    console.log(bytes.length) //10 
    */

    /*
    var express = require('express');
    var https = require('https');
    var http = require('http');
    var app = express();

    http.createServer(app).listen(80);
    https.createServer(options, app).listen(443);
    */

    app.listen(target_http_port, function() {
        console.log("doctor's app is listening to port " + target_http_port);
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
  secret: 'shhhh, very secret',
}));

// common log
// TODO finish this part
app.use(function(req, res,next) {
    console.log("");
    console.log("Access to " + req.url);
    console.log("Host name  : " + req.headers.host);
    console.log(req.headers);
    if(!req.session.hie_cookie) {
        res.locals.login_status = "Not logged in";
    }
    else {
        res.locals.login_status = "Logged as " + req.session.current_def;
    }
  
    var idx = req.headers.host.lastIndexOf(":");
    var port = req.headers.host.substring(idx + 1, req.headers.host.length);
    console.log("port number: " + port);
    //console.log("test time: " + req.session.cookie.maxAge);
    next();
});

app.use(app.router);

// index page
app.get('/', routes.index);

// handling reqest permission services
app.get('/req_permission_page', routes.req_permission_page);
app.post('/req_permission', routes.req_permission);

// handling issuing claims
app.post('/update_permission', routes.update_permission);

app.get('/check_access', routes.check_access);
app.get('/authenticate_hie_page', routes.authenticate_hie_page);
app.post('/authenticate_hie', routes.authenticate_hie);
app.get('/logout', routes.logout);

app.get('/get_data', routes.get_data);

app.get('/user/:id', function (req, res, next) {
  console.log('ID:', req.params.id);
  next();
}, function (req, res, next) {
});


// pages for seeing the patient's record
//app.get('/list_results');
//app.get('/show_something');

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
