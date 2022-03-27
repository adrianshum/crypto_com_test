Design Overview
---------------

This portfolio system is composed of several major modules

Instrument
===========
Instrument represents a financial instrument. Currently we support only Stock and
European Option.

Market Data
===========
Market Data represent the price we receive from market data provider.

Currently we have a Brownian-Motion-based Random Market Data Service, which
provide data to Market Data Channel for subscribers that is interested in Market Data Change

Pricing
========
The price of instrument our system is using to evaluate value of positions.

It could be based on market data directly (e.g. Stock), or calculated based on
market data (e.g. Derivatives calculated based on pricing of underlying), or based
on other pricing strategies (price given by internal trading desk? OTC? or markup market price?)

Currently we have a Pricing Service, which subscribe from Market Data Channel to receive
market data updates, and provide data to Pricing Channel for parties that is interested
in pricing change.

Portfolio
==========
Represents positions of different instrument holdings, and for evaluating the market
value of positions.

It is subscribing Pricing Change from Pricing Channel. When pricing change is received,
it re-evaluate market value for affected positions and notify the view for display.

Currently we have a Console Portfolio View which displays portfolio change to standard-output.



Technical Overview
------------------

Channel
=======
For demonstration purpose only, I haven't adopted a messaging framework yet. 
Market-Data-Channel and Pricing-Channel are implemented using simple Observer pattern.
However, the way it behaves is similar to commonly-seen log-based application.
When subscribing, subscriber will first receive events to allow them to construct 
the initial state of data.  After an initialization of end of such initial events 
(Snapshot End), subsequent events receives are real-time event. 

Tests
=====
Due to time deficit, Unit Tests are written for components that contains most business
logic, which is Portfolio, Pricing Service and European Option Pricing Strategy.
