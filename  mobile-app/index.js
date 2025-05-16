```javascript
/**
 * @format
 */

import {AppRegistry} from 'react-native';
import App from './src/App'; // Assuming App.tsx is in src folder
import {name as appName} from './app.json';

AppRegistry.registerComponent(appName, () => App);
```