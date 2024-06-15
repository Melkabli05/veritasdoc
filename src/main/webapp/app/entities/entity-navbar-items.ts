import NavbarItem from 'app/layouts/navbar/navbar-item.model';

export const EntityNavbarItems: NavbarItem[] = [
  {
    name: 'File',
    route: '/file',
    translationKey: 'global.menu.entities.file',
  },
  {
    name: 'FileVersion',
    route: '/file-version',
    translationKey: 'global.menu.entities.fileVersion',
  },
  {
    name: 'Metadata',
    route: '/metadata',
    translationKey: 'global.menu.entities.metadata',
  },
  {
    name: 'Tag',
    route: '/tag',
    translationKey: 'global.menu.entities.tag',
  },
  {
    name: 'Permission',
    route: '/permission',
    translationKey: 'global.menu.entities.permission',
  },
  {
    name: 'Folder',
    route: '/folder',
    translationKey: 'global.menu.entities.folder',
  },
];
