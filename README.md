# PointBox 2010
PointBox system is made up of PointBox-console (<strong>source opened by this repo</strong>), PointBoxLocal/PointBoxCentral servers (Data polling and web service endpoints e.g. authentication/authorization), PointBox Engine HTTP Server (Instant messaging collection, parsing, quote-formatting, pricing engine ), PointBox controller (i.e. pricing engine's support, e.g. data skew curves, AtmVolCurve, etc.), PBIM server (i.e. PointBox instant messaging server), and database with backup, etc. 

<img src="https://github.com/zhijun98/pointbox_2010/assets/9690419/b478a1aa-8ecc-4373-92ee-858ecf5f8314" width="600">

## PointBox Console (PBC)
<strong>This repository only opened the source for PointBox-console</strong> which is the front-end GUI client application of PointBox system. It was implemented as a classic Java Swing rich-client application to support traders and/or brokers's daily operations. 

(1) Commodity traders/brokers talks (or sends quotes on commodities) through Yahoo, AOL, and PointBox's instant-messaging which are provided by PointBox console (PBC) application. If traders/brokers use of PB's instant messaging, they will use of the following talker's panel to send their quotes:

<img src="https://github.com/zhijun98/pointbox_2010/assets/9690419/0fac8425-8040-4753-9208-8b52836e772c" width="600">

(2) PBC has ability of "catching and understanding" traders/brokers conversation by means of Yahoo/AOL instant messaging and PointBox's talker in the real time.

<img src="https://github.com/zhijun98/pointbox_2010/assets/9690419/3e082d65-ea80-451c-b30e-a983dafbdb56" width="600">
<img src="https://github.com/zhijun98/pointbox_2010/assets/9690419/4de232fc-5b2d-42c3-963a-bdfc10d0558b" width="600">

(3) PBC will automatically parse traders/brokers' conversation and convert them into a standard commodity option-quotes presented into "trade entry form":

![Trade Entry Form](https://github.com/zhijun98/pointbox_2010/assets/9690419/ec211eaa-768d-45ed-8336-4df57a059246)

(4) PBC ask PB-pricer to price such quotes and present the predicted PB-pricing-result into the following yellow-jacket-like tables:
![pb-grid](https://github.com/zhijun98/pointbox_2010/assets/9690419/17781e34-bf98-4f2a-82a9-31d6a1ba29d4)

(5) Traders/brokers do their trade by the help of PB-pricing results.

To sum up, tarders/broackers exchange commodity quotes over various instant messengers (AOL, Yahoo, PointBox) by means of PointBox console under the help of its backend servers (whose implementation is not presented in this repo). As soon as the instant messages containing quotes are sent out through PBC, it will be collected, parsed, formatted, priced, and presented immediately so that traders/brokers may made decision by the help of pricing result. As an integrated commodity option pricing and communication platform, PointBox system allows users to maximize their market focus while minimizing their time spent on manual processing. PointBox simplifies market pricing, market tracking, and communication by utilizing automatic quote parsing and a standardized option language. It helps traders and/or brokers to make trading decisions more efficiently. 

## Documents & Screen Shots
For more information on the entire PointBox system, especially its architecture, release and deployment environment, these two folders contains materials on them. 
