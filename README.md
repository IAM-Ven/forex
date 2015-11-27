Forex
=====

Running instance : https://srv.cloud-app.pw/

Frontend is using AngularJS with Highchart (Highstock), BootStrap and SockJS.

Backend is using Java 7 with Spring-boot 1.3.

SockJS is used for real-time rates chart rendering.

Forex data providers
--------
##### [Yahoo Finance](http://finance.yahoo.com/webservice/v1/symbols/allcurrencies/quote) (default)
Free, no registration, updated every 5 minutes, 172 currencies supported.

Response is XML format, but JSON is available with [`format=json`](http://finance.yahoo.com/webservice/v1/symbols/allcurrencies/quote?format=json) url parameter.

##### [European Central Bank](http://www.ecb.europa.eu/stats/exchange/eurofxref/html/index.en.html)
Free, no registration, updated daily at 3pm (CET).

Response is XML format.

##### [Open Exchange Rates](https://openexchangerates.org/documentation)
Free, registration required, updated hourly, 165 currencies supported.

Response is JSON format.
