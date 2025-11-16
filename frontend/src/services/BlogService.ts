import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { ResponseData } from "../app/types/responseData";
import { BlogItem, ProductItems } from "../app/types/productItem";
import { ApiResponse, Page } from "../model/product.model";

@Injectable({ providedIn: 'root' })
export class BlogService {
    private http= inject(HttpClient);
    constructor() {
        
    
    }

    getProducts(params?: any): Observable<ApiResponse<Page<ProductItems>>> {
        return this.http.get<ApiResponse<Page<ProductItems>>>(
            'http://localhost:8080/identity/product', 
            { params }
        );
    }

    detailBlog(id: number): Observable<ResponseData<ProductItems>>{
        return this.http.get<any>(`https://ninedev-api.vercel.app/blogs/${id}`)
    }

    postBlog(blogItem: BlogItem): Observable<ResponseData<ProductItems>>{
        return this.http.post<any>(`https://ninedev-api.vercel.app/blogs`, blogItem)
    }

    deleteBlog(id: number): Observable<ResponseData<ProductItems>>{
        return this.http.delete<any>(`https://ninedev-api.vercel.app/blogs/${id}`)
    }
}