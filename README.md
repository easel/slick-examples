# Based on https://danielwestheide.com/blog/2015/06/28/put-your-writes-where-your-master-is-compile-time-restriction-of-slick-effect-types.html

# Setup
create user "slick-examples" with password 'slick-examples';
create user "slick-examples-ro" with password 'slick-examples-ro';
create database "slick-examples" with owner "slick-examples";
