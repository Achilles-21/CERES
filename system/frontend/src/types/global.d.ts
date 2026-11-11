type Mitt ={
  on: (event: string, handler: Function)=>  void;
off: (event: string, handler: Function) =>  void;
emit: (event: string, payload?: any)=>  void ;
}
 interface CusProxy extends ComponentPublicInstance {
  $api: Api;
  $mitt: Mitt;
  $qf: (color: string, lighten: number) => string;
  $htor: (color: string, opacity: number) => string;
}
