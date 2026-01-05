
import { DeviceInformation } from './device-information'
import { AppiumSession } from './appium-session'
export class Device {

udid: string;
slaveIp: string;
deviceInformation: DeviceInformation ;
isFree: string;
user: string;
appiumUrl: string;
uniqueNumber: String;
appiumSession: AppiumSession;
available: boolean;
stfUrl: string;

}
