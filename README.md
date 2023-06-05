# PointBox 2010
As an integrated commodity option pricing and communication platform, PointBox system allows users to maximize their market focus while minimizing their time spent on manual processing. PointBox simplifies market pricing, market tracking, and communication by utilizing automatic quote parsing and a standardized option language. It helps traders and/or brokers to make trading decisions more efficiently. 

## Documents & Screen Shots
It contains the big picture of PointBox architecture, release with deployment enviroment, and screen shots on front-end GUI design, etc. 

## PointBox Console (PBC)
A classic Java Swing rich-client application which is the main front-end of PointBox trading system for traders and/or brokers. 

(1) Commodity traders/brokers talks (or sends quotes on commodities) through Yahoo, AOL, and PointBox's instant-messaging which are provided by PointBox console (PBC) application. If traders/brokers use of PB's instant messaging, they will use of the following talker's panel to send their quotes:
![pb-option-pricer](https://github.com/zhijun98/pointbox_2010/assets/9690419/0fac8425-8040-4753-9208-8b52836e772c)
(2) PBC has ability of "catching and understanding" traders/brokers conversation in the real time.

(3) PBC will automatically parse traders/brokers' conversation and convert them into a standard commodity option-quotes presented into "trade entry form" (refer to the following screen-shots) 

(4) PBC ask PB-pricer to price such quotes and present the predicted PB-pricing-result into the following yellow-jacket-like tables:

(5) Traders/brokers do their trade by the help of PB-pricing results.

Refer to screen-shots for more GUI design and implementation for client-side users. 
