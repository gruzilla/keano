var HDWalletProvider = require("truffle-hdwallet-provider");
var constants = require('./constants');

module.exports = {
  // See <http://truffleframework.com/docs/advanced/configuration>
  // to customize your Truffle configuration!
  networks: {
    development: {
      host: "127.0.0.1",
      port: 7545,
      network_id: "*"
    },
    ropsten: {
      provider: function() {
        return new HDWalletProvider(constants.MNEMONIC, "https://ropsten.infura.io/RJKIAWDX4HGB6G91RKTFDVGXC5RYM9VN5Z")
      },
      network_id: 3,
      gas: 4000000      //make sure this gas allocation isn't over 4M, which is the max
    }
  }
};