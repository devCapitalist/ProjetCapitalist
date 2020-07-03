import {
  Component,
  EventEmitter,
  Input,
  NgModule,
  OnChanges,
  OnInit,
  Output,
  ViewChild
} from "@angular/core";
import { RestserviceService } from '../restservice.service';

import { Pallier, Product } from "../world";




@Component({
  selector: 'app-product',
  templateUrl: './product.component.html',
  styleUrls: ['./product.component.scss']
})
export class ProductComponent implements OnInit {
  public product: Product;
  public progressbarvalue: number = 0;
  public timeleft: number = 0;
  public lastUpdate: number;
  public isUnlocked: boolean = false;
  public _qtmulti: any;
  public _money: any;
  public maxBuyableQuantity: number = 0;
  public priceToBuyProducts: number = 0;
  public quantityToBuy: number = 0;
  public pallier: Pallier;


  @Input()
  set prod(value: Product) { 
    this.product = value;
    if (this.product.quantite > 0) {
      this.isUnlocked = true;
    }
  }

  @Input()
  set money(value: any) { 
    this._money = value;
    if (this._qtmulti && this.product){
      this.maxBuyableQuantity = this.calcMaxCanBuy();
      this.setPriceToBuyProducts();
    } 
  }
 
  
 @Input()
 set qtmulti(value: any) {
 this._qtmulti = value;
  if (this._qtmulti && this.product) {
    this.maxBuyableQuantity = this.calcMaxCanBuy();
    this.setPriceToBuyProducts();
  }
 }

 @Input()
  set manager(value: Pallier) { 
    console.log("test" + value.name)

  }


  

  @Output() notifyProduction: EventEmitter<Product> = new
  EventEmitter<Product>();

  @Output() public notifyAchat = new EventEmitter();


  constructor() { }

  ngOnInit(): void {
    setInterval(() => { this.calcScore(); }, 33,333);
  }

  startProduction(){ 
    if (this.isUnlocked) {
      this.product.timeleft = this.product.vitesse;
      this.lastUpdate = Date.now();
    } else {
      window.alert("not unlocked")
    }

  }

  calcScore() {
    if (this.product.timeleft !== 0) {
      this.product.timeleft -= (Date.now() - this.lastUpdate) ;
      if (this.product.timeleft <= 0 ) { 
        this.product.timeleft = 0;
        this.progressbarvalue = 0;
        // on prévient le composant parent que ce produit a généré son revenu.
        this.notifyProduction.emit(this.product);
      } else {
        this.progressbarvalue = ((this.product.vitesse - this.product.timeleft) / this.product.vitesse) * 100
      }
    } 
  }

  public calcMaxCanBuy() {
    /**
     ** Calcul de la quantité maximal que l'on peut acheter.
     **   afin d'accelerer les calculs, on mise sur la mémoire.
     **   En stockant le prix du n-ième produit (prix_n), on calcul rapidement celui de n+1
     **   il suffit alors de les ajouter.
     */
    let quantite = 0;
    let prix_total = 0;
    let prix_n =
      this.product.cout * this.product.croissance ** this.product.quantite;
    while (prix_total < this._money) {
      // on ajoute au prix le n-ième produit.
      prix_total += prix_n;
      
      prix_n *= this.product.croissance;
      quantite++;
    }
    return quantite === 0 ? 0 : quantite - 1;
  }

  public buy() {
    this.product.quantite += this.quantityToBuy;
    let udpate = {
      "price": this.priceToBuyProducts,
      "product": this.product
    }
    this.notifyAchat.emit({"price": this.priceToBuyProducts, "product": this.product})
  }

  public setPriceToBuyProducts() {
    if(this._qtmulti === "MAX" || this._qtmulti >= this.maxBuyableQuantity) {
      this.priceToBuyProducts = this.product.cout * this.product.croissance ** (this.maxBuyableQuantity + this.product.quantite);
      this.quantityToBuy = this.maxBuyableQuantity;
    } else {
      this.priceToBuyProducts = this.product.cout * this.product.croissance ** (this._qtmulti + this.product.quantite);
      this.quantityToBuy = this._qtmulti;
    }
  }

  public unlockProduct(){
    if (this._money >= this.product.cout) {
      this.isUnlocked = true;
      this.product.quantite = 1;
      this.notifyAchat.emit({"price": this.product.cout, "product": this.product})

    } else {
      window.alert(" pas assez de personnes infectées")
    }
  }



}
