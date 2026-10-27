var q=document.getElementById("q"),P=new URLSearchParams(location.search);
function card(c){var a=document.createElement("article");a.className="card";
 var h=document.createElement("h3");h.textContent=c[0];var p=document.createElement("p");p.textContent=c[1];
 var m=document.createElement("div");m.className="meta";
 c.slice(2).forEach(function(x,i){var s=document.createElement("span");s.className="tag"+(i==2?(x=="Бесплатно"?" f":x=="Платно"?" p":""):"");s.textContent=x;m.appendChild(s)});
 var b=document.createElement("a");b.className="btn";b.href="index.html#apply";b.textContent="Записаться";a.append(h,p,m,b);return a}

/* поиск в шапке: на других страницах переводит в каталог */
document.querySelector(".search").addEventListener("submit",function(e){
 e.preventDefault();if(!document.getElementById("grid"))location.href="courses.html?q="+encodeURIComponent(q.value)});

/* главная */
var pop=document.getElementById("popular");
if(pop)[D.teen.c[0],D.student.c[1],D.pro.c[0],D.team.c[0]].forEach(function(c){pop.appendChild(card(c))});
var f=document.getElementById("f");
if(f)f.addEventListener("submit",function(e){e.preventDefault();document.getElementById("note").textContent="Заявка отправлена. Мы свяжемся с вами в течение двух рабочих дней.";e.target.reset()});

/* каталог */
var grid=document.getElementById("grid");
if(grid){
 var g0=P.get("g");var st={g:D[g0]?g0:"all",f:"",p:"",q:P.get("q")||""};q.value=st.q;
 var render=function(){
  var keys=st.g=="all"?Object.keys(D):[st.g],s=st.q.trim().toLowerCase(),n=0;grid.innerHTML="";
  keys.forEach(function(k){D[k].c.forEach(function(c){
   if(st.f&&c[3]!=st.f)return;if(st.p&&c[4]!=st.p)return;if(s&&(c[0]+" "+c[1]).toLowerCase().indexOf(s)<0)return;
   grid.appendChild(card(c));n++})});
  document.getElementById("ttl").textContent=s?"Результаты поиска":st.g=="all"?"Все курсы":D[st.g].t;
  document.getElementById("lead").textContent=s?"Запрос: "+st.q:st.g=="all"?"Выберите направление слева или найдите курс по названию.":D[st.g].l;
  if(!n){var e=document.createElement("p");e.className="lead";e.textContent="Ничего не нашли. Сбросьте фильтры или попробуйте слово «электрика» или «смета».";grid.appendChild(e)}
  document.querySelectorAll("#aud a").forEach(function(a){a.setAttribute("aria-selected",a.dataset.aud===st.g)});
  document.getElementById("ff").value=st.f;document.getElementById("fp").value=st.p};
 document.querySelectorAll("#aud a").forEach(function(a){a.addEventListener("click",function(e){
  e.preventDefault();st.g=a.dataset.aud;st.q="";q.value="";history.replaceState(null,"","courses.html?g="+st.g);render()})});
 document.getElementById("ff").addEventListener("change",function(e){st.f=e.target.value;render()});
 document.getElementById("fp").addEventListener("change",function(e){st.p=e.target.value;render()});
 q.addEventListener("input",function(){st.q=q.value;st.g="all";render()});
 render();
}
