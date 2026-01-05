import { Injectable } from '@angular/core';
import { Device } from './device'
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class DeviceService {

  private baseURL = "http://devicelab.games24x7.com:8088/actuator-slave/findAllDevices";

  constructor(private httpClient: HttpClient) { }

  getDevicesInfo(): Observable<Device[]>{
      return this.httpClient.get<Device[]>(`${this.baseURL}`);
    }
}
