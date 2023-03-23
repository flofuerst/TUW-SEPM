import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {environment} from 'src/environments/environment';
import {Horse, HorseSearch} from '../dto/horse';

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
   * Get all horses stored in the system which match with the specified data
   *
   * @return observable list of found horses.
   */
  searchHorses(horseToSearch: HorseSearch): Observable<Horse[]> {
    let params = new HttpParams();

    // delete data if HorseSearch-data is empty or null
    // only add to params if not undefined
    if (horseToSearch.name?.length === 0 || horseToSearch.name == null) {
      delete horseToSearch.name;
    } else if (horseToSearch.name !== undefined) {
      params = params.append('name', horseToSearch.name);
    }

    if (horseToSearch.description?.length === 0 || horseToSearch.description == null) {
      delete horseToSearch.description;
    } else if (horseToSearch.description !== undefined) {
      params = params.append('description', horseToSearch.description);
    }

    if (horseToSearch.bornBefore?.length === 0 || horseToSearch.bornBefore == null) {
      delete horseToSearch.bornBefore;
    } else if (horseToSearch.bornBefore !== undefined) {
      params = params.append('bornBefore', horseToSearch.bornBefore);
    }

    if (horseToSearch.sex?.length === 0 || horseToSearch.sex == null) {
      delete horseToSearch.sex;
    } else if (horseToSearch.sex !== undefined) {
      params = params.append('sex', horseToSearch.sex);
    }

    if (horseToSearch.ownerName?.length === 0 || horseToSearch.ownerName == null) {
      delete horseToSearch.ownerName;
    } else if (horseToSearch.ownerName !== undefined) {
      params = params.append('ownerName', horseToSearch.ownerName);
    }

    return this.http.get<Horse[]>(baseUri, {params});
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
