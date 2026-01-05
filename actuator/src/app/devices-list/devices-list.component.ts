import { Component, OnInit } from '@angular/core';
import { Device } from '../device'
import { DeviceService } from '../device.service'
import { Router } from '@angular/router';
import { Observable, Subscription, interval  } from 'rxjs';

@Component({
  selector: 'app-devices-list',
  templateUrl: './devices-list.component.html',
  styleUrls: ['./devices-list.component.css']
})
export class DevicesListComponent implements OnInit {
private updateSubscription: Subscription;

device : Device[];

  constructor(private deviceService: DeviceService) { }

  ngOnInit(): void {
  console.log("just testing");
      this.getDevices();
    }

//     private getDevices(){
//       this.deviceService.getDevicesInfo().subscribe(data => {
//         this.device = data;
//         });
//         console.log("data is "+this.device);
//     }

 private getDevices(){
      this.updateSubscription = interval(3000).subscribe(data => {
        this.updateStats();
        });
        console.log("data is "+this.device);
    }
     private updateStats(){
        this.deviceService.getDevicesInfo().subscribe(data => {
           this.device = data;
        });
       }
   }
