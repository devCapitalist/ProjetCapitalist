import { Component } from '@angular/core';
import { RestserviceService } from './restservice.service';
import { World, Pallier, Product} from './world'
import { from } from 'rxjs';



@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})



export class AppComponent {
  public title = 'virusCap';
  public world: World = new World();
  public service: RestserviceService;
  public server: string;
  public username: string;
  public qtmulti: any = 1;

  constructor(
    private restService: RestserviceService,
  ) {
    this.server = restService.getServer();
    restService.getWorld().then(
      world => {
        this.world = world;
        
      }
    )

  }

  onProductionDone(p: Product) {
    this.world.money += p.revenu * p.croissance;
  }

  onNotifyAchat(update: any) {
    this.world.money -= update.price;
    this.restService.putProduct(update.product);
  }

  public switchQtmulti(){
    console.log(this.qtmulti)
    switch (this.qtmulti) {
      case 1: 
        this.qtmulti = 10;
        break;

      case 10:
        this.qtmulti = 100;
        break;
        
      case 100: 
        this.qtmulti = "MAX";
        break;

      case "MAX": 
        this.qtmulti = 1;
        break;

    }
  }



  
  

}
