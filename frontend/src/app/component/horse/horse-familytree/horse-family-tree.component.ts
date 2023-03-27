import {Component, OnInit} from '@angular/core';
import {Horse, HorseFamilyTree} from '../../../dto/horse';
import {Sex} from '../../../dto/sex';
import {HorseService} from '../../../service/horse.service';
import {ActivatedRoute, Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {Observable} from 'rxjs';
import {HttpErrorResponse} from '@angular/common/http';

@Component({
  selector: 'app-horse-familytree',
  templateUrl: './horse-family-tree.component.html',
  styleUrls: ['./horse-family-tree.component.scss']
})
export class HorseFamilyTreeComponent implements OnInit {
  public horseFamilyTree?: Observable<HorseFamilyTree>;
  public maxGenerations = 3;
  horse: Horse = {
    name: '',
    description: '',
    // @ts-ignore
    dateOfBirth: null,
    sex: Sex.female
  };

  constructor(
    private service: HorseService,
    private router: Router,
    private route: ActivatedRoute,
    private notification: ToastrService,
  ) {
  }

  ngOnInit(): void {
    this.route.data.subscribe(data => {

      // retrieve data
      const horseId = Number(this.route.snapshot.paramMap.get('id'));

      if (Number.isInteger(horseId)) {
        const observable = this.service.get(horseId);

        observable.subscribe({
          next: horse => {
            this.horse = horse;
            this.loadFamilyTree();
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
    });
  }

  loadFamilyTree() {
    if (Number.isInteger(this.horse.id) && this.horse.id !== undefined) {
      this.horseFamilyTree = this.service.getFamilyTree(this.horse.id, this.maxGenerations);
    } else {
      this.notification.error(`Error while retrieving horse for family tree`);
      this.router.navigate(['/horses']);
      console.error(`Error getting horse for family tree with id
        "${this.route.snapshot.paramMap.get('id')}" (horse id invalid)`);
    }
  }

  deleteHorse(horse: Horse) {
    if (horse.id != null) {
      const observable = this.service.delete(horse.id);

      observable.subscribe({
        next: () => {
          this.loadFamilyTree();
          this.notification.success(`Horse ${horse.name} successfully deleted from family tree.`);
        },
        error: (error: HttpErrorResponse) => {
          this.notification.error(`Error while deleting ${horse.name} from family tree: ${error.error.errors}`);
        }
      });
    }
  }
}
