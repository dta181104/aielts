import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { ApiResponse, Page } from "../model/product.model";
import { ProductItems } from "../app/types/productItem";
import { environment } from "../environments/environment";

@Injectable({ providedIn: 'root' })
export class ProductService {
  private http = inject(HttpClient);
  private baseUrl = environment.apiUrl;

  getProducts(params?: any): Observable<ApiResponse<Page<ProductItems>>> {
    return this.http.get<ApiResponse<Page<ProductItems>>>(
      `${this.baseUrl}/product`, 
      { params }
    );
  }

  getProductById(id: string): Observable<ApiResponse<ProductItems>> {
    return this.http.get<ApiResponse<ProductItems>>(
      `${this.baseUrl}/product/${id}`
    );
  }
}
