import {AfterViewInit, Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {ToastrService} from 'ngx-toastr';
import {HorseService} from 'src/app/service/horse.service';
import {Horse, HorseSearch} from '../../dto/horse';
import {Owner} from '../../dto/owner';
import {HttpErrorResponse} from '@angular/common/http';
import {OwnerService} from 'src/app/service/owner.service';
import {debounceTime, of, Subscription} from 'rxjs';

@Component({
  selector: 'app-horse',
  templateUrl: './horse.component.html',
  styleUrls: ['./horse.component.scss']
})
export class HorseComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('inputForm', {static: true}) form?: NgForm;
  search = false;
  horses: Horse[] = [];
  bannerError: string | null = null;
  horseToSearch: HorseSearch = {};
  inputFormUpdate?: Subscription;

  constructor(
    private service: HorseService,
    private ownerService: OwnerService,
    private notification: ToastrService,
  ) {
  }

  ngOnInit(): void {
    this.reloadHorses();
  }


  ngAfterViewInit(): void {
    this.inputFormUpdate = this.form?.valueChanges?.pipe(
      debounceTime(500)
    ).subscribe(() => this.reloadHorses());
  }

  ngOnDestroy(): void {
    this.inputFormUpdate?.unsubscribe();
  }

  reloadHorses() {
    this.service.searchHorses(this.horseToSearch)
      .subscribe({
        next: data => {
          this.horses = data;
        },
        error: error => {
          console.error('Error fetching horses', error);
          this.bannerError = 'Could not fetch horses: ' + error.message;
          const errorMessage = error.status === 0
            ? 'Is the backend up?'
            : error.message.message;
          this.notification.error(errorMessage, 'Could Not Fetch Horses');
        }
      });
  }

  deleteHorse(horse: Horse): void {
    if (horse.id != null) {
      const observable = this.service.delete(horse.id);

      observable.subscribe({
        next: () => {
          this.reloadHorses();
          this.notification.success(`Horse ${horse.name} successfully deleted.`);
        },
        error: (error: HttpErrorResponse) => {
          this.notification.error(`Error while deleting ${horse.name}: ${error.error.errors}`);
        }
      });
    }
  }

  ownerName(owner: Owner | null): string {
    return owner
      ? `${owner.firstName} ${owner.lastName}`
      : '';
  }

  public formatObjectOwnerName(owner: Owner | null | undefined): string {
    return (owner == null)
      ? ''
      : `${owner.firstName} ${owner.lastName}`;
  }

  public formatStringOwnerName(ownerName: string | null | undefined): string {
    return (ownerName == null)
      ? ''
      : ownerName;
  }

  ownerSuggestions = (input: string) => (input === '')
    ? of([])
    : this.ownerService.searchByName(input, 5);

  dateOfBirthAsLocaleDate(horse: Horse): string {
    return new Date(horse.dateOfBirth).toLocaleDateString();
  }
}
