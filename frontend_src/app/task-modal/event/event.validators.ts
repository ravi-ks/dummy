import { AbstractControl,ValidationErrors } from '@angular/forms';
export class EventValidators{
    static noGreaterStartTime(startTime : string){
        return (control : AbstractControl) : ValidationErrors | null =>{
        let endTime=control.value as string;
        startTime.toLowerCase().replace(' ','');
        endTime.toLowerCase().replace(' ','');
        if(startTime.localeCompare(endTime)<=0){
          return {noGreaterStartTime : false};
        }
        return {noGreaterStartTime : true};
      };
    }




    static noSmallerEndTime(endTime : string){
      return (control : AbstractControl) : ValidationErrors | null =>{
      let startTime=control.value as string;
      startTime.toLowerCase().replace(' ','');
      endTime.toLowerCase().replace(' ','');
      if(startTime.localeCompare(endTime)<=0){
        return {noGreaterStartTime : false};
      }
      return {noGreaterStartTime : true};
    };
  }




}
