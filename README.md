# keano

## READ ME

you need `nvm`. run it to setup the pre-requisites:

    nvm use
    npm install

please inspect `package.json` and its run-scripts.

## connect with your wallet

copy `metacoin/constants.dist.js` to `metacoin/constants.js` and
fill in your wallets mnemonic.

## use the console with ropsten:

    npm run ropstenConsole

## deploy to ropsten:

    npm run compile
    npm run ropstenDeploy

## eth sink

https://faucet.metamask.io/

## setup

you shouldn't have to do this, because we checked
the metacoin directory into git.

to re-create the initial 'metacoin' directory in its initial state
(without ecr20 compat) run:

    npm run remove
    npm run setup
