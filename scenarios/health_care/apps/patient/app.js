var express = require('express');
var http = require('http');
var path = require('path');
var favicon = require('static-favicon');
var logger = require('morgan');
var cookieParser = require('cookie-parser');
var bodyParser = require('body-parser');

var routes = require('./routes');
var users = require('./routes/user');

var app = express();
if(process.argv.length == 2) {
    app.listen(3001, function() {
        app.this_http_port = 3001;
        console.log("Patient's app is listening to port 3001");
    });
}
else if(process.argv.length == 3) {
    var target_http_port = parseInt(process.argv[2]);
    app.this_http_port = target_http_port;
    console.log("HTTP Port : " + target_http_port);

    app.listen(target_http_port, function() {
        console.log("Patient's app is listening to port " + target_http_port);
    });
}

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'jade');

app.use(favicon());
app.use(logger('dev'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded());
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));
app.use(app.router);

// index page
app.get('/', routes.index);

// claim definition management
app.get('/add_claimdef_page', routes.add_claimdef_page);
app.post('/add_claimdef', routes.add_claimdef);
app.get('/list_claimdef', routes.list_claimdef);

// handling user relevant service
app.get('/list_user', routes.list_user);
app.post('/add_user', routes.add_user);

// handling issuing claims
app.get('/allow_permission_page', routes.allow_permission_page);
app.post('/allow_permission', routes.allow_permission);

app.get('/revocate_user_page', routes.revocate_user_page);
app.post('/revocate_user', routes.revocate_user);

app.get('/cert/', routes.cert);
app.get('/claims', routes.claims);
// TODO change name...?
app.post('/claim_service', routes.issue_claim);

app.get('/claimdef/:id', routes.claimdef);
app.get('/param_names/:id', routes.param_names);

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
