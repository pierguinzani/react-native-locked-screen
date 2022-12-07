import { EmitterSubscription, LogBox, NativeEventEmitter, NativeModules } from 'react-native';
import { isObject, log, setDebugMode } from './util';
const { RNLocked } = NativeModules;
LogBox.ignoreLogs(['new NativeEventEmitter']);

const eventEmitter = new NativeEventEmitter(RNLocked);
const eventMap = {};

export const getRegisteredEventNames = () => {
  return Object.keys(eventMap);
};

export const removeEvent = (eventName: string) => {
  if (getRegisteredEventNames().includes(eventName)) {
    eventMap[eventName].remove();
    delete eventMap[eventName];
    log.d(`removeEvent: event ${eventName} is removed.`);
  } else {
    throw new Error(`${eventName} 이벤트는 react-native-locked에 등록되지 않았습니다.`);
  }
};

export const getEventHandler = (eventName) => {
  return isObject(eventMap) && eventMap[eventName];
};

export const showLog = setDebugMode;

export const setEvent = (eventName: string, handler: (event: any) => void): EmitterSubscription => {
  if (getRegisteredEventNames().includes(eventName)) {
    log.d(`setEvent: event ${eventName} already registered.`);
    removeEvent(eventName);
  }
  eventMap[eventName] = eventEmitter.addListener(eventName, handler);
  return eventMap[eventName];
};

export const setEvents = (events) => {
  Object.entries(events).forEach(([k, v]) => setEvent(k, v));
};

// export const isScreenOff = () => {
//   return RNLocked.isScreenOff();
// };

export const setOnBeforeExit = (onBeforeExit?: () => void) => {
  if (onBeforeExit) {
    setEvent('onBeforeExit', onBeforeExit);
  }
}

export const exitApp = () => {
  const onBeforeExit = eventMap['onBeforeExit'];
  RNLocked.exitApp(onBeforeExit);
}

export default RNLocked;
