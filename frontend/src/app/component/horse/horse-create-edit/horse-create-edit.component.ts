import {Component, OnInit} from '@angular/core';
import {NgForm, NgModel} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {Observable, of} from 'rxjs';
import {Horse} from 'src/app/dto/horse';
import {Owner} from 'src/app/dto/owner';
import {Sex} from 'src/app/dto/sex';
import {HorseService} from 'src/app/service/horse.service';
import {OwnerService} from 'src/app/service/owner.service';
import {HttpErrorResponse} from '@angular/common/http';


export enum HorseCreateEditMode {
  create,
  edit,
};

@Component({
  selector: 'app-horse-create-edit',
  templateUrl: './horse-create-edit.component.html',
  styleUrls: ['./horse-create-edit.component.scss']
})
export class HorseCreateEditComponent implements OnInit {

  mode: HorseCreateEditMode = HorseCreateEditMode.create;
  horse: Horse = {
    name: '',
    description: '',
    // @ts-ignore
    dateOfBirth: null,
    sex: Sex.female,
  };


  constructor(
    private service: HorseService,
    private ownerService: OwnerService,
    private router: Router,
    private route: ActivatedRoute,
    private notification: ToastrService,
  ) {
  }

  public get heading(): string {
    switch (this.mode) {
      case HorseCreateEditMode.create:
        return 'Create New Horse';
      case HorseCreateEditMode.edit:
        return 'Update Horse';
      default:
        return '?';
    }
  }

  public get submitButtonText(): string {
    switch (this.mode) {
      case HorseCreateEditMode.create:
        return 'Create';
      case HorseCreateEditMode.edit:
        return 'Update';
      default:
        return '?';
    }
  }

  get modeIsCreate(): boolean {
    return this.mode === HorseCreateEditMode.create;
  }


  private get modeActionFinished(): string {
    switch (this.mode) {
      case HorseCreateEditMode.create:
        return 'created';
      case HorseCreateEditMode.edit:
        return 'updated';
      default:
        return '?';
    }
  }

  ownerSuggestions = (input: string) => (input === '')
    ? of([])
    : this.ownerService.searchByName(input, 5);

  ngOnInit(): void {
    this.route.data.subscribe(data => {
      this.mode = data.mode;

      if (this.mode === HorseCreateEditMode.edit) {
        const horseId = Number(this.route.snapshot.paramMap.get('id'));

        if (Number.isInteger(horseId)) {
          const observable = this.service.get(horseId);

          observable.subscribe({
            next: horse => {
              this.horse = horse;
            },
            error: error => {
              this.notification.error(`Error while retrieving horse`);
              this.router.navigate(['/horses']);
              console.error(`Error getting horse with id "${this.route.snapshot.paramMap.get('id')}" (horse id not found)`, error);
              return;
            }
          });
        } else {
          this.notification.error(`Error while retrieving horse`);
          this.router.navigate(['/horses']);
          console.error(`Error getting horse with id "${this.route.snapshot.paramMap.get('id')}" (horse id invalid)`);
          return;
        }
      }
    });
  }

  public dynamicCssClassesForInput(input: NgModel): any {
    return {
      // This names in this object are determined by the style library,
      // requiring it to follow TypeScript naming conventions does not make sense.
      // eslint-disable-next-line @typescript-eslint/naming-convention
      'is-invalid': !input.valid && !input.pristine,
    };
  }

  public formatOwnerName(owner: Owner | null | undefined): string {
    return (owner == null)
      ? ''
      : `${owner.firstName} ${owner.lastName}`;
  }

  public onDelete(): void {
    if (!this.modeIsCreate && this.horse.id != null) {
      const observable = this.service.delete(this.horse.id);

      observable.subscribe({
        next: () => {
          this.notification.success(`Horse ${this.horse.name} successfully deleted.`);
          this.router.navigate(['/horses']);
        },
        error: (error: HttpErrorResponse) => {
          this.notification.error(`Error while deleting ${this.horse.name}: ${error.error.errors}`);
          this.router.navigate(['/horses']);
          console.error(`Error while deleting ${this.horse.name}: ${error.error.errors}`);
        }
      });
    }
  }

  public onSubmit(form: NgForm): void {
    console.log('is form valid?', form.valid, this.horse);
    if (form.valid) {
      if (this.horse.description === '') {
        delete this.horse.description;
      }
      let observable: Observable<Horse>;
      switch (this.mode) {
        case HorseCreateEditMode.create:
          observable = this.service.create(this.horse);
          break;
        case HorseCreateEditMode.edit:
          observable = this.service.update(this.horse);
          break;
        default:
          console.error('Unknown HorseCreateEditMode', this.mode);
          return;
      }
      observable.subscribe({
        next: data => {
          this.notification.success(`Horse ${this.horse.name} successfully ${this.modeActionFinished}.`);
          this.router.navigate(['/horses']);
        },
        error: error => {
          if (this.mode === HorseCreateEditMode.edit) {
            this.notification.error(`Error while updating ${this.horse.name}: ${error.error.errors}`);
          } else {
            this.notification.error(`Error while creating ${this.horse.name}: ${error.error.errors}`);
          }
          console.error('Error creating horse', error);
        }
      });
    }
  }

}
