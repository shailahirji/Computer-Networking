This program creates a web directory treating webfoot as the source directory. 

It handles GET and HEAD requests and responds with HTTP 1.1 response headers. 

When GET request is made, the body of the object is part of the response. If request is HEAD, only the main header fields are returned with no body.  

The program handles 404 errors for when a page that doesn't exist is requested for 