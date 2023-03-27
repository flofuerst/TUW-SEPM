import {Component, Input} from '@angular/core';
import {HorseFamilyTreeComponent} from '../horse-family-tree.component';
import {Horse, HorseFamilyTree} from '../../../../dto/horse';

@Component({
  selector: 'app-horse-familytree-node',
  templateUrl: './horse-family-tree-node.component.html',
  styleUrls: ['./horse-family-tree-node.component.scss']
})
export class HorseFamilyTreeNodeComponent {
  isExpanded?: boolean;

  constructor(
    private familyTree: HorseFamilyTreeComponent,
  ) {
    this.isExpanded = false;
  }

  @Input()
  horse?: HorseFamilyTree;

  delete(horse: Horse) {
    this.familyTree.deleteHorse(horse);
  }

  changeExpandState() {
    this.isExpanded = !this.isExpanded;
  }

  dateOfBirthAsLocaleDate(horse: Horse): string {
    return new Date(horse.dateOfBirth).toLocaleDateString();
  }

  sexToSymbol(horse: Horse): string {
    return horse.sex === 'FEMALE' ? 'bi-gender-female' : 'bi-gender-male';
  }
}
