import {Component} from '@angular/core';
import {NgForm, NgModel} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {Owner} from 'src/app/dto/owner';
import {OwnerService} from 'src/app/service/owner.service';
import {HttpErrorResponse} from '@angular/common/http';

@Component({
  selector: 'app-owner-create',
  templateUrl: './owner-create.component.html',
  styleUrls: ['./owner-create.component.scss']
})
export class OwnerCreateComponent {
  owner: Owner = {
    firstName: '',
    lastName: '',
    email: ''
  };

  constructor(
    private ownerService: OwnerService,
    private router: Router,
    private route: ActivatedRoute,
    private notification: ToastrService,
  ) {
  }

  public dynamicCssClassesForInput(input: NgModel): any {
    return {
      // This names in this object are determined by the style library,
      // requiring it to follow TypeScript naming conventions does not make sense.
      // eslint-disable-next-line @typescript-eslint/naming-convention
      'is-invalid': !input.valid && !input.pristine,
    };
  }

  public onSubmit(form: NgForm): void {
    console.log('is form valid?', form.valid, this.owner);
    if (form.valid) {
      if (this.owner.email === '') {
        delete this.owner.email;
      }
      const observable = this.ownerService.create(this.owner);
      observable.subscribe({
        next: () => {
          this.notification.success(`Owner
            ${this.owner.firstName + ' ' + this.owner.lastName} successfully created.`);
          this.router.navigate(['/owners']);
        },
        error: (error: HttpErrorResponse) => {
          this.notification.error(`Error while creating Owner
            ${this.owner.firstName + ' ' + this.owner.lastName}: ${error.error.errors}`);
          console.error('Error creating horse', error);
        }
      });
    }
  }

}
