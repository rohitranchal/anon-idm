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
app.listen(3001, function() {
    console.log("Patient's app is listening to port 3001");
});

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

app.get('/cert/', routes.cert);
app.get('/claims', routes.claims);
app.post('/claim_service', routes.issue_claim);

//app.get('/claimdef_show', routes.claimdef_show);
//app.get('/req_claim', routes.req_claim);
app.get('/users', users.list);

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
