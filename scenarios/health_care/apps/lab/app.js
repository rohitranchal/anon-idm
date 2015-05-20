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
    app.listen(3003, function() {
        app.this_http_port = 3003;
        console.log("Lab's app is listening to port 3003");
    });
}
else if(process.argv.length == 3) {
    var target_http_port = parseInt(process.argv[2]);
    app.this_http_port = target_http_port;
    console.log("HTTP Port : " + target_http_port);

    app.listen(target_http_port, function() {
        console.log("Lab's app is listening to port " + target_http_port);
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

app.get('/', routes.index);
app.get('/register_record_page', routes.register_record_page);
app.post('/register_record', routes.register_record);

app.get('/update_record_page', routes.update_record_page);
app.post('/update_record', routes.update_record);

app.get('/send_record_page', routes.send_record_page);
app.post('/send_record', routes.send_record);

/*
app.get('/list_record');
*/

app.get('/get_parameters', routes.get_parameters);

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
