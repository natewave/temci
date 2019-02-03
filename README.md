# Introduction

Temci is a web app that let's you schedule Ethereum Transactions to be executed at a later time, in a decentralized and trustless way. It uses the [Ethereum Alarm Clock](https://www.ethereum-alarm-clock.com) project and is entirely written in [ClojureScript](https://clojurescript.org).

🚨 This is being done for demonstration/learning purposes, use at your own risk 🚨 

## Live demo
You can try a live demo of the app here: https://natewave.github.io/temci/

(you can also see gifs of the app in action [here](screens/walkthrough.md))

## Run on your own

Clone the repository

```bash
> git clone git@github.com:natewave/temci.git
```

Install deps

```bash
temci> yarn install
```

Run in dev mode

```bash
temci> yarn run
```

The app should be available at `localhost:3000`

To compile an optimized version, run:
```bash
temci> yarn release
```

## Testing
You can use the Kovan test network in [Metamask](https://metamask.io) to test out the app.

To get free Kovan: https://github.com/kovan-testnet/faucet

To explore Kovan: https://kovan.etherscan.io

# Contribute

Temci* is written in ClojureScript using the awesome [re-frame](https://github.com/Day8/re-frame) framework with [shadow-cljs](https://github.com/thheller/shadow-cljs) as the build tool.

(* temci is a [lojban](https://en.wikipedia.org/wiki/Lojban) word that means Time)
# License
```
Copyright (c) Nizar S. (nisehl@gmail.com). All rights reserved.
The use and distribution terms for this software are covered by the Eclipse
Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
which can be found in the file epl-v10.html at the root of this
distribution. By using this software in any fashion, you are
agreeing to be bound by the terms of this license. You must
not remove this notice, or any other, from this software.
```