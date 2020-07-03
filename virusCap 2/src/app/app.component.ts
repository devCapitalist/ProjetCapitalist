import { Component } from '@angular/core';
import { RestserviceService } from './restservice.service';
import { World, Pallier, Product} from './world'
import { from } from 'rxjs';
import { ProductComponent } from './product/product.component';
import { MatSnackBar } from '@angular/material/snack-bar';




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

  showManagers = false;
  badgeManagers = 0;

  showUnlocks = false;

 


  constructor(
    private restService: RestserviceService,
    private snackBar: MatSnackBar
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
  toggleManager(): void {
    this.showManagers;
    this.showUnlocks;
  }

  nextUnlocks(product?: Product): Pallier {
    let pallier: Pallier[];
    if (product == null) {
      pallier = this.world.allunlocks.pallier;
    } else {
      pallier = product.palliers.pallier;
    }
    for (let i = 0; i < pallier.length - 1; i++) {
      if (!pallier[i].unlocked) {
        return pallier[i];
      }
    }
    return null;
  }

  hireManager(manager: Pallier): void {
    if (this.world.money < manager.seuil) {
      return;
    }

    this.service.putManager(manager).then(() => {
      this.world.money -= manager.seuil;
      manager.unlocked = true;
      this.world.products.product[manager.idcible - 1].managerUnlocked = true;
      this.snackBar.open(manager.name + ' just joinned your universe', '', {
        
        duration: 4000,
      });
      console.log(manager.name);
    });
  }



  switchShowUnlocks() {
    this.showUnlocks = !this.showUnlocks;
  }

  switchShowManagers() {
    console.log(this.world.managers.pallier)
    this.showManagers = !this.showManagers;
  }
  

}
