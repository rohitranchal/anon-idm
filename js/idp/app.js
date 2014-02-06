
/**
 * Module dependencies.
 */

var express = require('express')
  , routes = require('./routes')
  , http = require('http')
  , path = require('path');

var app = express();

app.configure(function(){
  app.set('port', process.env.PORT || 3000);
  app.set('views', __dirname + '/views');
  app.set('view engine', 'jade');
  app.use(express.favicon());
  app.use(express.logger('dev'));
  app.use(express.bodyParser());
  app.use(express.methodOverride());
  app.use(app.router);
  app.use(express.static(path.join(__dirname, 'public')));
});

app.configure('development', function(){
  app.use(express.errorHandler());
});

app.get('/', routes.index);
app.get('/cert/', routes.cert);

app.get('/claimdef_show', routes.claimdef_show);
app.post('/claimdef_process', routes.claimdef_process);
app.get('/claims/', routes.claims);
app.get('/claim/:id', routes.claim);
app.get('/claimdef/:id', routes.claimdef);

app.post('/claim_service', routes.issue_claim);

app.get('/useradd_show', routes.useradd_show);
app.post('/useradd_process', routes.useradd_process);
app.get('/users', routes.users);

app.get('/admin', routes.admin_show);


http.createServer(app).listen(app.get('port'), function(){
  console.log("Express server listening on port " + app.get('port'));
});
