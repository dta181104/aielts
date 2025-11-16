import { Component, NgModule, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterOutlet } from '@angular/router';
import { BlogItem, ProductItems } from '../types/productItem';
import { BlogService } from '../../services/BlogService';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-create',
  standalone: true,
  imports: [ReactiveFormsModule, NgIf],
  templateUrl: './create.component.html',
  styleUrls: ['./create.component.css'],
})
export class CreateComponent {

  product = new FormGroup({
    name: new FormControl('', Validators.required),
    price: new FormControl('', Validators.required),
  })

  get name(){
    return this.product.get('name');
  }
  get price(){
    return this.product.get('price');
  }
  constructor(private blogService: BlogService, private router: Router) {}

  handleAddCart() {
    if(this.name?.hasError('required') || this.price?.hasError('required')){
      return;
    }
    const blogItem: BlogItem = {
      id: Math.random(),
      title: String(this.name?.value),
      body: String(this.price?.value),
      author: 'OK'
    }
    this.blogService.postBlog(blogItem).subscribe(({ data }: any) => {
      if(data.id){
        this.router.navigate(['/']);
      }
    })
  }
}
