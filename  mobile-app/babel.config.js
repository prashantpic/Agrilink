```javascript
module.exports = {
  presets: ['module:@react-native/babel-preset'],
  plugins: [
    ['@babel/plugin-proposal-decorators', {legacy: true}],
    'react-native-reanimated/plugin', // Reanimated plugin needs to be listed last.
  ],
};
```