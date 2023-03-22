import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {environment} from 'src/environments/environment';
import {Horse} from '../dto/horse';

const baseUri = environment.backendUrl + '/horses';

@Injectable({
  providedIn: 'root'
})
export class HorseService {

  constructor(
    private http: HttpClient,
  ) {
  }

  /**
   * Get all horses stored in the system
   *
   * @return observable list of found horses.
   */
  getAll(): Observable<Horse[]> {
    return this.http.get<Horse[]>(baseUri);
  }

  /**
   * Get a horse by its specified id
   *
   * @param id the id of the wanted horse
   * @return an Observable for the horse with specified id
   */
  get(id: number): Observable<Horse> {
    return this.http.get<Horse>(`${baseUri}/${id}`);
  }

  /**
   * Update an existing horse in the system
   *
   * @param horse the data for the horse that should be updated
   * @return an Observable for the updated horse
   */
  update(horse: Horse): Observable<Horse> {
    return this.http.put<Horse>(
      `${baseUri}/${horse.id}`,
      horse
    );
  }

  /**
   * Create a new horse in the system.
   *
   * @param horse the data for the horse that should be created
   * @return an Observable for the created horse
   */
  create(horse: Horse): Observable<Horse> {
    return this.http.post<Horse>(
      baseUri,
      horse
    );
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${baseUri}/${id}`);
  }

  public searchByName(name: string, limitTo: number): Observable<Horse[]> {
    const params = new HttpParams()
      .set('name', name)
      .set('maxAmount', limitTo);
    return this.http.get<Horse[]>(baseUri, {params});
  }

}
