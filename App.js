import React, {Component} from 'react';
import {Platform, StyleSheet, Text, View} from 'react-native';
import BillingLib from './src/BillingLib';

const instructions = Platform.select({
  ios: 'Press Cmd+R to reload,\n' + 'Cmd+D or shake for dev menu',
  android:
    'Double tap R on your keyboard to reload,\n' +
    'Shake or press menu button for dev menu',
});

type Props = {};
export default class App extends Component<Props> {

  componentDidMount() {
  }

  async onTextPressed() {
    await BillingLib.closeConnection();
    try {
      await BillingLib.openConnection();
      BillingLib.queryPurchaseHistoryAsync("inapp").then(list => {
        console.log(list);
      }).error(err => {
        console.log(err);
      });
    } catch(err) {
      console.log(err);
    } finally {
      await BillingLib.closeConnection();
    }
  }

  render() {
    return (
      <View style={styles.container}>
        <Text style={styles.welcome}>Welcome to React Native!</Text>
        <Text style={styles.instructions}>To get started, edit App.js</Text>
        <Text style={styles.instructions}>{instructions}</Text>

        <Text onPress={() => this.onTextPressed()}>Press me</Text>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
});
