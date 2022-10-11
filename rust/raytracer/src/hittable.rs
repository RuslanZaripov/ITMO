use crate::ray::Ray;
use crate::Vec3;
use crate::vec3::dot;

#[derive(Debug, Copy, Clone)]
pub struct HitRecord {
    pub t: f64,
    pub p: Vec3,
    pub normal: Vec3,
    pub front_face: bool
}

impl HitRecord {
    pub fn new(t: f64, p: Vec3, normal: Vec3, front_face: bool) -> HitRecord {
        HitRecord { t, p, normal, front_face }
    }

    pub fn set_face_normal(&mut self, ray: &Ray, outward_normal: &Vec3) {
        self.front_face = dot(&ray.dir, outward_normal) < 0.0;
        self.normal = if self.front_face { *outward_normal } else { -(*outward_normal) };
    }
}

pub trait Hittable {
    fn hit(&self, ray: &Ray, t_min: f64, t_max: f64) -> Option<HitRecord>;
}

pub struct HittableList {
    objects: Vec<Box<dyn Hittable>>
}

impl HittableList {
    pub fn new() -> HittableList {
        HittableList { objects: Vec::new() }
    }

    pub fn clear(&mut self) {
        self.objects.clear();
    }

    pub fn add(&mut self, object: impl Hittable + 'static) {
        self.objects.push(Box::new(object));
    }
}

impl Hittable for HittableList {
    fn hit(&self, ray: &Ray, t_min: f64, t_max: f64) -> Option<HitRecord> {
        let mut hit_rec = None;
        let mut dist_to_closest = t_max;
        for object in &self.objects {
            if let Some(rec) = object.hit(ray, t_min, dist_to_closest) {
                dist_to_closest = rec.t;
                hit_rec = Some(rec);
            }
        }
        hit_rec
    }
}
