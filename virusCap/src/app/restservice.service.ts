import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from "@angular/common/http";
import { Pallier, Product, World } from "./world";


@Injectable({
  providedIn: 'root'
})
export class RestserviceService {

  public server = "http://localhost:8080/"
  public user = "Rayane"

  constructor(private http: HttpClient) { }

  private handleError(error: any): Promise<any> {
    return Promise.reject(error.message || error)
  }

  public getWorld(): Promise<World> {
    return this.http
      .get(this.server + "adventureisis/generic/world", {
        headers: { "X-user": this.user }
      })
      .toPromise()
      .then(response => response)
      .catch(this.handleError);
  }

  public getServer(): string { 
    return this.server;
  }

  public putProduct(product: Product): Promise<Response> {
    console.log(product)
    
    return this.http
      .put(this.server + "adventureisis/generic/product", product, {
        headers: { "X-user": this.user }
      })
      .toPromise()
      .then(response => response)
      .catch(this.handleError);
  }

}
