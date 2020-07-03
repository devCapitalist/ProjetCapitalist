import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { HttpClientModule } from "@angular/common/http";

import { AppComponent } from './app.component';
import { ProductComponent } from './product/product.component';
import { BigvaluePipe } from './bigvalue.pipe';

import {MatProgressBarModule} from '@angular/material/progress-bar';

import { BrowserAnimationsModule } from '@angular/platform-browser/animations'
import { from } from 'rxjs';
import { MaterialModule } from './material.module';

@NgModule({
  declarations: [
    AppComponent,
    ProductComponent,
    BigvaluePipe
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    MatProgressBarModule,
    BrowserAnimationsModule,
    MaterialModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
