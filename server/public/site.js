// LocalCart site: sticky header state, the "how an order works" phone demo, and the metadata example.
(() => {
  const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  const desktop = window.matchMedia('(min-width: 961px)');

  const nav = document.querySelector('[data-nav]');
  const onScroll = () => nav?.classList.toggle('is-scrolled', window.scrollY > 24);
  window.addEventListener('scroll', onScroll, { passive: true });
  onScroll();

  // ---- Phone demo -------------------------------------------------------------------------
  const phone = document.querySelector('[data-phone]');
  const steps = [...document.querySelectorAll('.step[data-step]')];
  if (phone && steps.length) {
    const reveals = [...phone.querySelectorAll('.reveal[data-show]')];
    const typewriter = phone.querySelector('[data-typewriter]');
    const typing = phone.querySelector('[data-typing]');
    const cartPill = phone.querySelector('[data-cart-pill]');
    const track = [...phone.querySelectorAll('[data-track] li')];
    const stageLabel = phone.querySelector('[data-stage-label]');
    const fullText = typewriter?.textContent ?? '';
    let current = 0;
    let typed = false;
    let stageTimer = 0;

    const typeOnce = () => {
      if (typed || !typewriter || reduceMotion) return;
      typed = true;
      typewriter.textContent = '';
      typewriter.classList.add('is-typing');
      let i = 0;
      const tick = () => {
        typewriter.textContent = fullText.slice(0, ++i);
        if (i < fullText.length) setTimeout(tick, 28 + Math.random() * 40);
        else typewriter.classList.remove('is-typing');
      };
      setTimeout(tick, 250);
    };

    const runStages = (on) => {
      clearInterval(stageTimer);
      const labels = ['Accepted', 'Packed', 'Out for delivery', 'Delivered'];
      let stage = on ? 1 : 0;
      const paint = () => {
        track.forEach((li, i) => li.classList.toggle('done', i < stage));
        if (stageLabel) stageLabel.textContent = labels[Math.max(stage, 1) - 1];
      };
      paint();
      if (!on) return;
      if (reduceMotion) { stage = 4; paint(); return; }
      stageTimer = setInterval(() => {
        stage = stage >= 4 ? 1 : stage + 1;
        paint();
      }, 1400);
    };

    const setStep = (n) => {
      if (n === current) return;
      current = n;
      phone.dataset.step = String(n);
      steps.forEach((s) => s.classList.toggle('is-active', Number(s.dataset.step) === n));
      reveals.forEach((r) => r.classList.toggle('is-shown', Number(r.dataset.show) <= n));
      if (cartPill) cartPill.textContent = n >= 3 ? 'Billed' : 'Draft';
      if (typing) typing.textContent = n === 2 ? 'typing…' : 'Open now';
      if (n === 1) typeOnce();
      runStages(n === 4);
    };

    // Desktop: the step in the middle of the viewport drives the sticky phone.
    let observer = null;
    const watchScroll = () => {
      observer?.disconnect();
      if (!desktop.matches) return;
      observer = new IntersectionObserver(
        (entries) => entries.forEach((e) => e.isIntersecting && setStep(Number(e.target.dataset.step))),
        { rootMargin: '-45% 0px -45% 0px' },
      );
      steps.forEach((s) => observer.observe(s));
    };

    // Small screens: the phone sits above the steps and plays through them while it's on screen.
    let autoTimer = 0;
    let pausedUntil = 0;
    let phoneVisible = false;
    new IntersectionObserver((entries) => {
      phoneVisible = entries[0].isIntersecting;
      if (phoneVisible && current === 0) setStep(1);
    }, { threshold: 0.35 }).observe(phone);
    const autoplay = () => {
      clearInterval(autoTimer);
      if (desktop.matches || reduceMotion) return;
      autoTimer = setInterval(() => {
        if (!phoneVisible || Date.now() < pausedUntil) return;
        setStep(current >= 4 ? 1 : current + 1);
      }, 4500);
    };

    steps.forEach((s) => {
      s.querySelector('.step-tab')?.addEventListener('click', () => {
        const n = Number(s.dataset.step);
        if (desktop.matches) s.scrollIntoView({ behavior: reduceMotion ? 'auto' : 'smooth', block: 'center' });
        else pausedUntil = Date.now() + 12000;
        setStep(n);
      });
    });

    desktop.addEventListener('change', () => { watchScroll(); autoplay(); });
    watchScroll();
    autoplay();
    if (desktop.matches) setStep(1);
  }

  // ---- Metadata example: the status value changes in place, like the card does --------------
  const swap = document.querySelector('[data-swap]');
  if (swap && !reduceMotion) {
    const values = swap.dataset.swap.split(',');
    let i = 0;
    let timer = 0;
    new IntersectionObserver((entries) => {
      clearInterval(timer);
      if (!entries[0].isIntersecting) return;
      timer = setInterval(() => {
        i = (i + 1) % values.length;
        swap.textContent = values[i];
        swap.classList.add('is-flash');
        setTimeout(() => swap.classList.remove('is-flash'), 450);
      }, 1800);
    }, { threshold: 0.6 }).observe(swap);
  }
})();
